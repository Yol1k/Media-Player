package com.example.mediaplayer.domain.models

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String = "Неизвестно",
    val path: String,
)
