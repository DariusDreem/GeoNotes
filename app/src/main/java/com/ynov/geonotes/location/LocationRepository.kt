package com.ynov.geonotes.location

import android.content.Context
import android.location.Location

/** Point d'accès unique à la position de l'utilisateur. */
class LocationRepository(private val context: Context) {

    fun hasPermission(): Boolean = LocationProvider.hasPermission(context)

    suspend fun currentLocation(): Location? = LocationProvider.currentLocation(context)
}