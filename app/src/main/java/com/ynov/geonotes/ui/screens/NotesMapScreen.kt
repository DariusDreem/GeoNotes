package com.ynov.geonotes.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ynov.geonotes.data.Note
import com.ynov.geonotes.ui.components.OsmMap

@Composable
fun NotesMapScreen(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        OsmMap(notes = notes, onNoteClick = onNoteClick, modifier = Modifier.fillMaxSize())
        if (notes.none { it.hasLocation }) {
            Surface(
                modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 4.dp,
                shadowElevation = 4.dp,
            ) {
                Text("Aucune note géolocalisée", Modifier.padding(12.dp))
            }
        }
    }
}
