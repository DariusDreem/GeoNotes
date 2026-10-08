package com.ynov.geonotes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    /** Chemin absolu de la photo dans le stockage interne de l'app. */
    val photoPath: String?,
    /** Date de création en millisecondes (epoch). */
    val date: Long,
    val latitude: Double?,
    val longitude: Double?,
) {
    val hasLocation: Boolean get() = latitude != null && longitude != null
}
