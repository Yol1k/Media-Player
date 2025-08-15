package com.example.mediaplayer.presentation.viewmodel

import android.app.Application
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

    private val _playlist = MutableStateFlow<List<Song>>(emptyList())

    private val _currentSongIndex = MutableStateFlow<Int?>(null)

    private val _repeatMode = MutableStateFlow(RepeatMode.NONE)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

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
                Player.STATE_IDLE -> { stopProgressUpdates() }
                Player.STATE_BUFFERING -> { stopProgressUpdates() }
                Player.STATE_READY -> {
                    _duration.value = exoPlayer.duration
                    if (exoPlayer.isPlaying) startProgressUpdates()
                }
                Player.STATE_ENDED -> {
                    when (_repeatMode.value) {
                        RepeatMode.ONE -> {
                            exoPlayer.seekTo(0)
                            exoPlayer.play()
                        }

                        RepeatMode.ALL -> skipToNext()
                        RepeatMode.NONE -> { }
                    }
                }
            }
        }
    }

    fun toggleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.NONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.NONE
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
        val dur = exoPlayer.duration.coerceAtLeast(1L)
        val currentPos = exoPlayer.currentPosition.coerceIn(0, dur)
        if (dur > 0) {
            _currentPosition.value = currentPos
            _progress.value = currentPos.toFloat() / dur
        }
    }

    fun seekTo(position: Float) {
        val dur = exoPlayer.duration
        if (dur <= 0) return
        val newPos = (position * dur).toLong().coerceIn(0, dur)
        _currentPosition.value = newPos
        _progress.value = position

        viewModelScope.launch {
            exoPlayer.seekTo(newPos)
        }
    }

    fun setPlaylist(songs: List<Song>) {
        _playlist.value = songs.toList()
    }

    fun skipToNext() {
        val currentIndex = _currentSongIndex.value ?: return
        val playlist = _playlist.value

        when {
            playlist.isEmpty() -> return
            currentIndex < playlist.lastIndex -> playSongAtIndex(currentIndex + 1)
            else -> playSongAtIndex(0)
        }
    }

    fun skipToPrevious() {
        val currentIndex = _currentSongIndex.value ?: return
        val playlist = _playlist.value

        when {
            playlist.isEmpty() -> return
            currentIndex > 0 -> playSongAtIndex(currentIndex - 1)
            else -> playSongAtIndex(playlist.lastIndex)
        }
    }

    private fun playSongAtIndex(index: Int) {
        val song = _playlist.value.getOrNull(index) ?: return
        _currentSongIndex.value = index
        playSong(song)
    }

    fun playSong(song: Song) {
        _currentSongIndex.value = _playlist.value.indexOfFirst { it.id == song.id }.takeIf { it != -1 }
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

enum class RepeatMode { NONE, ALL, ONE }