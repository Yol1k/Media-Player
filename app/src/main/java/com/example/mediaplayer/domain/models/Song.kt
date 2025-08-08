package com.example.mediaplayer.domain.models

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String = "Неизвестно",
    val path: String,
    val albumId: Long,
    val cover: Uri? = null,
)
