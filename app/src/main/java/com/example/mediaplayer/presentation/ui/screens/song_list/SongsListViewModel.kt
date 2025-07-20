package com.example.mediaplayer.presentation.ui.screens.song_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaplayer.data.local.LocalAudioDataSource
import com.example.mediaplayer.domain.models.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SongListViewModel(private val audioDataSource: LocalAudioDataSource) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs

    init {
        loadSongs()
    }

    fun loadSongs() {
        viewModelScope.launch {
            _songs.value = audioDataSource.getLocalSongs()
        }
    }
}