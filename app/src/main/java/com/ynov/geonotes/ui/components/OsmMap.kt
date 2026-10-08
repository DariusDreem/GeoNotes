package com.ynov.geonotes.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ynov.geonotes.data.Note
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private val DEFAULT_CENTER = GeoPoint(46.6, 2.4) // France
private const val DEFAULT_ZOOM = 5.5
private const val SINGLE_NOTE_ZOOM = 15.0

/**
 * Carte OpenStreetMap (osmdroid) affichant un marqueur par note géolocalisée.
 * La caméra est cadrée sur les notes au premier affichage uniquement.
 */
@Composable
fun OsmMap(
    notes: List<Note>,
    modifier: Modifier = Modifier,
    onNoteClick: ((Note) -> Unit)? = null,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val located = notes.filter { it.hasLocation }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
            controller.setZoom(DEFAULT_ZOOM)
            controller.setCenter(DEFAULT_CENTER)
        }
    }
    val state = remember { object { var framed = false } }

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            map.overlays.removeAll { it is Marker }
            located.forEach { note ->
                val marker = Marker(map).apply {
                    position = GeoPoint(note.latitude!!, note.longitude!!)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = note.title
                    if (onNoteClick != null) {
                        setOnMarkerClickListener { _, _ -> onNoteClick(note); true }
                    } else {
                        setOnMarkerClickListener { _, _ -> true }
                    }
                }
                map.overlays.add(marker)
            }
            if (!state.framed && located.isNotEmpty()) {
                state.framed = true
                frame(map, located)
            }
            map.invalidate()
        },
    )
}

private fun frame(map: MapView, notes: List<Note>) {
    val points = notes.map { GeoPoint(it.latitude!!, it.longitude!!) }
    if (points.size == 1) {
        map.controller.setZoom(SINGLE_NOTE_ZOOM)
        map.controller.setCenter(points.first())
        return
    }
    val box = BoundingBox.fromGeoPointsSafe(points)
    // Le cadrage nécessite que la vue soit mesurée.
    map.post {
        if (box.latitudeSpan < 1e-4 && box.longitudeSpanWithDateLine < 1e-4) {
            map.controller.setZoom(SINGLE_NOTE_ZOOM)
            map.controller.setCenter(box.centerWithDateLine)
        } else {
            map.zoomToBoundingBox(box, false, 120)
        }
    }
}
