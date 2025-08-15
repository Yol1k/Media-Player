package com.example.mediaplayer.domain.models

import com.example.mediaplayer.presentation.viewmodel.RepeatMode

data class PlayerState(
    val currentSong: Song? = null,
    val playlist: List<Song> = emptyList(),
    val currentSongIndex: Int? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val isSeeking: Boolean = false
)
