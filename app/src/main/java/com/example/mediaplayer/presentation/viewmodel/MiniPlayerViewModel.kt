package com.example.mediaplayer.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import com.example.mediaplayer.domain.models.PlayerState
import com.example.mediaplayer.domain.models.Song
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@UnstableApi
class MiniPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(application)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        MIN_BUFFER_MS,
                        MAX_BUFFER_MS,
                        BUFFER_FOR_PLAYBACK_MS,
                        BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
                    )
                    .setPrioritizeTimeOverSizeThresholds(true)
                    .build()
            )
            .setSeekParameters(SeekParameters.EXACT)
            .build()
            .apply {
                addListener(playerListener)
                setHandleAudioBecomingNoisy(true)
            }
    }

    private var progressUpdateJob: Job? = null
    private var wasPlayingBeforeSeek = false

    companion object {
        private const val MIN_BUFFER_MS = 15000
        private const val MAX_BUFFER_MS = 30000
        private const val BUFFER_FOR_PLAYBACK_MS = 1000
        private const val BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 2000
        private const val PROGRESS_UPDATE_INTERVAL_MS = 16L
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateState { copy(isPlaying = isPlaying) }
            if (isPlaying) {
                startProgressUpdates()
            } else {
                stopProgressUpdates()
            }
        }

        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_READY -> {
                    val duration = exoPlayer.duration.coerceAtLeast(1)
                    updateState {
                        copy(
                            duration = duration,
                            progress = exoPlayer.currentPosition.toFloat() / duration
                        )
                    }
                    if (exoPlayer.isPlaying) startProgressUpdates()
                }
                Player.STATE_ENDED -> handlePlaybackEnded()
                Player.STATE_IDLE -> stopProgressUpdates()
                Player.STATE_BUFFERING -> {}
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            stopProgressUpdates()
        }
    }

    fun playSong(song: Song) {
        val currentIndex = _playerState.value.playlist.indexOfFirst { it.id == song.id }
            .takeIf { it != -1 }

        updateState {
            copy(
                currentSong = song,
                currentSongIndex = currentIndex
            )
        }

        viewModelScope.launch {
            exoPlayer.run {
                stop()
                clearMediaItems()
                setMediaItem(MediaItem.fromUri(song.path))
                prepare()
                play()
            }
        }
    }

    fun seekTo(position: Float) {
        val duration = exoPlayer.duration
        if (duration <= 0) return

        wasPlayingBeforeSeek = exoPlayer.isPlaying
        updateState { copy(isSeeking = true) }

        exoPlayer.seekTo((position * duration).toLong())
        if (wasPlayingBeforeSeek) {
            exoPlayer.play()
        }

        updateState {
            copy(
                progress = position,
                isSeeking = false
            )
        }
    }

    fun skipToNext() {
        playerState.value.currentSongIndex?.let { currentIndex ->
            val nextIndex = if (currentIndex < playerState.value.playlist.lastIndex) {
                currentIndex + 1
            } else {
                if (playerState.value.repeatMode == RepeatMode.ALL) 0 else return
            }
            playSongAtIndex(nextIndex)
        }
    }

    fun skipToPrevious() {
        playerState.value.currentSongIndex?.let { currentIndex ->
            val prevIndex = if (currentIndex > 0) {
                currentIndex - 1
            } else {
                if (playerState.value.repeatMode == RepeatMode.ALL) {
                    playerState.value.playlist.lastIndex
                } else {
                    return
                }
            }
            playSongAtIndex(prevIndex)
        }
    }

    fun setPlaylist(songs: List<Song>) {
        updateState { copy(playlist = songs) }
    }

    fun toggleRepeatMode() {
        updateState {
            copy(
                repeatMode = when (repeatMode) {
                    RepeatMode.NONE -> RepeatMode.ALL
                    RepeatMode.ALL -> RepeatMode.ONE
                    RepeatMode.ONE -> RepeatMode.NONE
                }
            )
        }
    }

    private fun playSongAtIndex(index: Int) {
        playerState.value.playlist.getOrNull(index)?.let { song ->
            playSong(song)
        }
    }

    private fun handlePlaybackEnded() {
        when (playerState.value.repeatMode) {
            RepeatMode.ONE -> {
                exoPlayer.seekTo(0)
                exoPlayer.play()
            }
            RepeatMode.ALL -> skipToNext()
            RepeatMode.NONE -> skipToNext()
        }
    }

    private fun startProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (true) {
                updateProgress()
                delay(PROGRESS_UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    private fun updateProgress() {
        val duration = exoPlayer.duration
        if (duration > 0) {
            val position = exoPlayer.currentPosition.coerceIn(0, duration)
            updateState {
                copy(
                    currentPosition = position,
                    progress = position.toFloat() / duration
                )
            }
        }
    }

    private inline fun updateState(transform: PlayerState.() -> PlayerState) {
        _playerState.update(transform)
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