package com.ynov.geonotes.ui

import java.text.DateFormat
import java.util.Date
import java.util.Locale

fun formatDate(millis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(Date(millis))

fun formatCoordinates(latitude: Double, longitude: Double): String =
    String.format(Locale.US, "%.5f, %.5f", latitude, longitude)
