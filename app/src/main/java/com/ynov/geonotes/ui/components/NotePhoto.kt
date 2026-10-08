package com.ynov.geonotes.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import java.io.File

@Composable
fun NotePhoto(
    path: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    AsyncImage(
        model = File(path),
        contentDescription = "Photo de la note",
        contentScale = contentScale,
        modifier = modifier,
    )
}
