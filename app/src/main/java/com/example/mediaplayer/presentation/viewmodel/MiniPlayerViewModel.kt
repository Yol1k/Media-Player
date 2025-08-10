package com.example.mediaplayer.presentation.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.mediaplayer.domain.models.Song
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MiniPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _playlist = mutableStateListOf<Song>()

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(application).build().apply {
            addListener(playerListener)
        }
    }

    private var progressUpdateJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            if (isPlaying) startProgressUpdates() else stopProgressUpdates()
        }

        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_READY -> {
                    _duration.value = exoPlayer.duration
                    if (exoPlayer.isPlaying) startProgressUpdates()
                }
                Player.STATE_ENDED -> stopProgressUpdates()
            }
        }
    }

    private fun startProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (true) {
                updateProgress()
                delay(16)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    private fun updateProgress() {
        val currentPos = exoPlayer.currentPosition
        val dur = exoPlayer.duration
        if (dur > 0) {
            _currentPosition.value = currentPos
            _progress.value = currentPos.toFloat() / dur
        }
    }

    fun seekTo(position: Float) {
        if (exoPlayer.duration > 0) {
            val newPosition = (position * exoPlayer.duration).toLong()
            exoPlayer.seekTo(newPosition)
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
        stopProgressUpdates()
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
        super.onCleared()
    }
}