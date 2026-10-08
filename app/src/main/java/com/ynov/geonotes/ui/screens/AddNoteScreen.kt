package com.ynov.geonotes.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ynov.geonotes.location.LocationProvider
import com.ynov.geonotes.ui.components.NotePhoto
import com.ynov.geonotes.ui.formatCoordinates
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ynov.geonotes.ui.AddNoteViewModel
import com.ynov.geonotes.ui.LocationStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AddNoteViewModel = viewModel(factory = AddNoteViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    fun cancel() {
        viewModel.cancel()
        onCancel()
    }

    // Quand le ViewModel signale que la note est enregistrée, on quitte l'écran.
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    // --- Position ---
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        viewModel.onLocationPermissionResult(granted = result.values.any { it })
    }

    LaunchedEffect(Unit) {
        if (uiState.locationStatus == LocationStatus.Loading && !viewModel.hasLocationPermission()) {
            permissionLauncher.launch(LocationProvider.permissions)
        }
    }

    // --- Photo ---
    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> viewModel.onPhotoTaken(success) }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.onPhotoPicked(uri) }

    BackHandler { cancel() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouvelle note") },
                navigationIcon = {
                    IconButton(onClick = { cancel() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Annuler")
                    }
                },
                actions = {
                    IconButton(
                        enabled = uiState.canSave,
                        onClick = { viewModel.save() },
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Enregistrer")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Titre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.content,
                onValueChange = viewModel::onContentChange,
                label = { Text("Contenu") },
                minLines = 5,
                modifier = Modifier.fillMaxWidth(),
            )

            // Photo
            val currentPhoto = uiState.photoPath
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (currentPhoto != null) {
                    NotePhoto(currentPhoto, Modifier.fillMaxWidth().height(240.dp))
                } else {
                    Text("Aucune photo", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { takePicture.launch(viewModel.createCameraUri()) },
                ) { Text("Prendre une photo") }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        pickImage.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                ) { Text("Galerie") }
            }

            // Position
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    when (uiState.locationStatus) {
                        LocationStatus.Loading -> "Localisation en cours…"
                        LocationStatus.Found -> formatCoordinates(uiState.latitude!!, uiState.longitude!!)
                        LocationStatus.Denied -> "Permission de localisation refusée"
                        LocationStatus.Unavailable -> "Position indisponible (GPS activé ?)"
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (uiState.locationStatus == LocationStatus.Loading) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = {
                        if (viewModel.hasLocationPermission()) viewModel.refreshLocation()
                        else permissionLauncher.launch(LocationProvider.permissions)
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Actualiser la position")
                    }
                }
            }
        }
    }
}
