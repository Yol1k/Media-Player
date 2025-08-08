package com.example.mediaplayer.presentation.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.mediaplayer.domain.models.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MiniPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _playlist = mutableStateListOf<Song>()

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(application).build().apply {
            addListener(playerListener)
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
    }

    fun setPlaylist(songs: List<Song>) {
        _playlist.clear()
        _playlist.addAll(songs)
    }

    fun skipToNext() {
        val current = _currentSong.value ?: return
        val currentIndex = _playlist.indexOfFirst { it.id == current.id }

        if (currentIndex == -1) return

        val nextIndex = if (currentIndex < _playlist.lastIndex) currentIndex + 1 else 0
        _playlist.getOrNull(nextIndex)?.let { nextSong ->
            playSong(nextSong)
        }
    }

    fun skipToPrevious() {
        val current = _currentSong.value ?: return
        val currentIndex = _playlist.indexOfFirst { it.id == current.id }

        if (currentIndex == -1) return

        val prevIndex = if (currentIndex > 0) currentIndex - 1 else _playlist.lastIndex
        _playlist.getOrNull(prevIndex)?.let { prevSong ->
            playSong(prevSong)
        }
    }

    fun playSong(song: Song) {
        if (!_playlist.contains(song)) {
            _playlist.clear()
            _playlist.add(song)
        }

        viewModelScope.launch {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            _currentSong.value = song
            exoPlayer.setMediaItem(MediaItem.fromUri(song.path))
            exoPlayer.prepare()
            exoPlayer.play()
            _isPlaying.value = true
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