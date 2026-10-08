package com.ynov.geonotes.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ynov.geonotes.GeoNotesApplication
import com.ynov.geonotes.data.Note
import com.ynov.geonotes.data.NoteRepository
import com.ynov.geonotes.location.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class LocationStatus { Loading, Found, Denied, Unavailable }

/** Tout ce que l'écran d'ajout affiche, réuni dans un seul objet. */
data class AddNoteUiState(
    val title: String = "",
    val content: String = "",
    val photoPath: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationStatus: LocationStatus = LocationStatus.Loading,
    val isSaved: Boolean = false,
) {
    val canSave: Boolean get() = title.isNotBlank()
}

class AddNoteViewModel(
    private val noteRepository: NoteRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddNoteUiState())
    val uiState: StateFlow<AddNoteUiState> = _uiState.asStateFlow()

    /** Fichier donné à l'appareil photo, en attente de sa réponse. */
    private var pendingCameraFile: File? = null

    init {
        // Le ViewModel survit à la rotation : ce bloc ne s'exécute qu'une fois par ouverture de l'écran.
        if (locationRepository.hasPermission()) refreshLocation()
    }

    // --- Texte ---

    fun onTitleChange(title: String) = _uiState.update { it.copy(title = title) }

    fun onContentChange(content: String) = _uiState.update { it.copy(content = content) }

    // --- Position ---

    fun hasLocationPermission(): Boolean = locationRepository.hasPermission()

    fun onLocationPermissionResult(granted: Boolean) {
        if (granted) refreshLocation()
        else _uiState.update { it.copy(locationStatus = LocationStatus.Denied) }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(locationStatus = LocationStatus.Loading) }
            val location = locationRepository.currentLocation()
            _uiState.update {
                if (location != null) {
                    it.copy(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        locationStatus = LocationStatus.Found,
                    )
                } else {
                    it.copy(locationStatus = LocationStatus.Unavailable)
                }
            }
        }
    }

    // --- Photo ---

    /** Prépare un fichier vide et renvoie l'adresse à donner à l'appareil photo. */
    fun createCameraUri(): Uri {
        val file = noteRepository.newPhotoFile()
        pendingCameraFile = file
        return noteRepository.uriFor(file)
    }

    fun onPhotoTaken(success: Boolean) {
        val file = pendingCameraFile ?: return
        pendingCameraFile = null
        if (success) replacePhoto(file.absolutePath) else noteRepository.deletePhoto(file.absolutePath)
    }

    fun onPhotoPicked(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            noteRepository.importPhoto(uri)?.let { replacePhoto(it) }
        }
    }

    private fun replacePhoto(newPath: String) {
        val oldPath = _uiState.value.photoPath
        if (oldPath != null && oldPath != newPath) noteRepository.deletePhoto(oldPath)
        _uiState.update { it.copy(photoPath = newPath) }
    }

    // --- Enregistrer / annuler ---

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            noteRepository.add(
                Note(
                    title = state.title.trim(),
                    content = state.content.trim(),
                    photoPath = state.photoPath,
                    date = System.currentTimeMillis(),
                    latitude = state.latitude,
                    longitude = state.longitude,
                )
            )
            // On ne prévient l'écran qu'une fois la note réellement écrite en base.
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    /** Annulation : on supprime la photo prise pour ne pas laisser de fichier orphelin. */
    fun cancel() {
        noteRepository.deletePhoto(_uiState.value.photoPath)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as GeoNotesApplication
                AddNoteViewModel(app.noteRepository, app.locationRepository)
            }
        }
    }
}