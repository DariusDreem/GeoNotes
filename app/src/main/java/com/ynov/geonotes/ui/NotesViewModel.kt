package com.ynov.geonotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ynov.geonotes.GeoNotesApplication
import com.ynov.geonotes.data.Note
import com.ynov.geonotes.data.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(private val repository: NoteRepository) : ViewModel() {

    val notes: StateFlow<List<Note>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun note(id: Long): Flow<Note?> = repository.getById(id)

    fun deleteNote(note: Note) {
        viewModelScope.launch { repository.delete(note) }
    }

    companion object {
        /** Explique à Android comment construire ce ViewModel (avec son repository). */
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as GeoNotesApplication
                NotesViewModel(app.noteRepository)
            }
        }
    }
}