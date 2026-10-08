package com.ynov.geonotes

import android.app.Application
import com.ynov.geonotes.data.NoteRepository
import com.ynov.geonotes.location.LocationRepository

/**
 * Classe créée par Android au démarrage de l'app, avant tout écran.
 * Elle crée les repositories une seule fois ; les ViewModels viennent les chercher ici.
 */
class GeoNotesApplication : Application() {
    val noteRepository by lazy { NoteRepository(this) }
    val locationRepository by lazy { LocationRepository(this) }
}