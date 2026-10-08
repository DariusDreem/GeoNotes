package com.ynov.geonotes.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

/** Point d'accès unique aux notes : base de données Room + fichiers photo. */
class NoteRepository(private val context: Context) {

    private val dao = NoteDatabase.get(context).noteDao()

    fun getAll(): Flow<List<Note>> = dao.getAll()

    fun getById(id: Long): Flow<Note?> = dao.getById(id)

    suspend fun add(note: Note) {
        dao.insert(note)
    }

    suspend fun delete(note: Note) {
        dao.delete(note)
        deletePhoto(note.photoPath)
    }

    // --- Photos ---

    fun newPhotoFile(): File = PhotoStorage.newPhotoFile(context)

    fun uriFor(file: File): Uri = PhotoStorage.uriFor(context, file)

    /** Copie une image de la galerie dans l'app. Lecture/écriture disque, donc sur le thread IO. */
    suspend fun importPhoto(uri: Uri): String? = withContext(Dispatchers.IO) {
        PhotoStorage.importFromUri(context, uri)?.absolutePath
    }

    fun deletePhoto(path: String?) = PhotoStorage.delete(path)
}