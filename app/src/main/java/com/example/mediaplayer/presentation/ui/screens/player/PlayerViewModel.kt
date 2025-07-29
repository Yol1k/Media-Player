package com.example.mediaplayer.presentation.ui.screens.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.mediaplayer.domain.models.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(application).build().apply {
            addListener(playerListener)
        }
    }

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
    }

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    fun playSong(song: Song) {
        viewModelScope.launch {
            _currentSong.value = song
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                exoPlayer.setMediaItem(MediaItem.fromUri(song.path))
                exoPlayer.prepare()
                exoPlayer.play()
            }
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    override fun onCleared() {
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
        super.onCleared()
    }
}