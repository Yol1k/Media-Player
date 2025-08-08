package com.example.mediaplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaplayer.data.local.LocalAudioDataSource
import com.example.mediaplayer.domain.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SongListViewModel(private val audioDataSource: LocalAudioDataSource) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    fun loadSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            _songs.value = audioDataSource.getLocalSongs()
        }
    }

}