package com.example.mediaplayer.presentation.screen

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.mediaplayer.domain.models.Song
import com.example.mediaplayer.presentation.viewmodel.MiniPlayerViewModel
import com.example.mediaplayer.presentation.theme.MediaPlayerTheme
import com.example.mediaplayer.presentation.viewmodel.SongListViewModel

@Composable
fun SongListScreen(
    songViewModel: SongListViewModel,
    miniPlayerViewModel: MiniPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val songs by songViewModel.songs.collectAsState()
    val currentSong by miniPlayerViewModel.currentSong.collectAsState()
    val isPlaying by miniPlayerViewModel.isPlaying.collectAsState()
    
    var showFullScreenPlayer by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        songViewModel.loadSongs()
    }

    LaunchedEffect(songs) {
        if (songs.isNotEmpty()) {
            miniPlayerViewModel.setPlaylist(songs)
        }
    }

    Box(modifier = modifier.fillMaxSize(),
    ) {
        // Основной контент - список песен
        AnimatedVisibility(
            visible = !showFullScreenPlayer,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (currentSong != null) 72.dp else 0.dp)
            ) {
                items(
                    items = songs,
                    key = { it.id }
                ) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = currentSong?.id == song.id,
                        onPlayClick = {
                            if (currentSong?.id == song.id) miniPlayerViewModel.togglePlayPause()
                            else { miniPlayerViewModel.playSong(song) }
                                      },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Мини-плеер
        currentSong?.let { song ->
            AnimatedVisibility(
                visible = !showFullScreenPlayer,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(animationSpec = tween(durationMillis = 300)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                MiniPlayer(
                    song = song,
                    isPlaying = isPlaying,
                    onPlayPause = { miniPlayerViewModel.togglePlayPause() },
                    onPlayerClick = { showFullScreenPlayer = true },
                    onSkipToNextClick = { miniPlayerViewModel.skipToNext() },
                    onSkipToPreviousClick = { miniPlayerViewModel.skipToPrevious() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp)
                )
            }
        }

        // Полноэкранный плеер
        AnimatedVisibility(
            visible = showFullScreenPlayer,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(animationSpec = tween(durationMillis = 300)),
            modifier = Modifier.zIndex(1f)
        ) {
            currentSong?.let { song ->
                PlayerScreen(
                    song = song,
                    isPlaying = isPlaying,
                    onPlayPause = { miniPlayerViewModel.togglePlayPause() },
                    onClose = { showFullScreenPlayer = false },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onSkipToNextClick: () -> Unit,
    onSkipToPreviousClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable { onPlayerClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            AlbumCover(
                uri = song.cover,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = song.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = onSkipToPreviousClick) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Включить предыдущий трек"
            )
        }

        IconButton(onClick = onPlayPause) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        IconButton(onClick = onSkipToNextClick) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Включить следующий трек"
            )
        }
    }
}

@Composable
private fun SongListItem(
    song: Song,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumCover(
            uri = song.cover,
            modifier = Modifier.size(48.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = song.title,
                fontWeight = FontWeight.Bold,
                color = if (isPlaying) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = song.artist,
                color = if (isPlaying) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onPlayClick) {
            Icon(Icons.Default.MoreVert, "Действия")
        }
    }
}

@Composable
fun AlbumCover(
    uri: Uri?,
    modifier: Modifier = Modifier.size(48.dp),
    shape: Shape = CircleShape
) {
    uri?.let {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = modifier.clip(shape),
            contentScale = ContentScale.Crop
        )
    } ?: Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = "Нет обложки"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SongListItemPreview() {
    MediaPlayerTheme {
        SongListItem(
            song = Song(
                id = 1,
                title = "Bohemian Rhapsody",
                artist = "Queen",
                album = "A Night at the Opera",
                path = "",
                albumId = 123,
                cover = Uri.parse("content://media/external/audio/albumart/123")
            ),
            isPlaying = true,
            onPlayClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MiniPlayerPreview() {
    MediaPlayerTheme {
        MiniPlayer(
            song = Song(
                id = 2,
                title = "Yesterday",
                artist = "The Beatles",
                path = "",
                albumId = 456,
                cover = null
            ),
            isPlaying = false,
            onPlayPause = {},
            onPlayerClick = {},
            onSkipToNextClick = {},
            onSkipToPreviousClick = {},
        )
    }
}



