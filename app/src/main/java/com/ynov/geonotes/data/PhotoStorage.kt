package com.ynov.geonotes.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/** Gère les photos stockées dans le répertoire interne `files/photos`. */
object PhotoStorage {

    private fun photosDir(context: Context): File =
        File(context.filesDir, "photos").apply { mkdirs() }

    /** Crée un fichier vide destiné à recevoir une photo de l'appareil photo. */
    fun newPhotoFile(context: Context): File =
        File(photosDir(context), "${UUID.randomUUID()}.jpg")

    fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    /** Copie une image choisie dans la galerie vers le stockage interne. */
    fun importFromUri(context: Context, uri: Uri): File? {
        val target = newPhotoFile(context)
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            target
        }.getOrNull()
    }

    fun delete(path: String?) {
        if (path != null) File(path).delete()
    }
}
