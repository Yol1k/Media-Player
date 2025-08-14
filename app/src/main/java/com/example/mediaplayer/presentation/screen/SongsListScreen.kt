package com.example.mediaplayer.presentation.screen

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.mediaplayer.domain.models.Song
import com.example.mediaplayer.presentation.viewmodel.MiniPlayerViewModel
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

    Box(modifier = modifier.fillMaxSize()) {
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
                            if (currentSong?.id == song.id) {
                                miniPlayerViewModel.togglePlayPause()
                            } else {
                                miniPlayerViewModel.playSong(song)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        currentSong?.let { song ->
            AnimatedVisibility(
                visible = !showFullScreenPlayer,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                MiniPlayer(
                    song = song,
                    isPlaying = isPlaying,
                    onPlayPause = { miniPlayerViewModel.togglePlayPause() },
                    onPlayerClick = { showFullScreenPlayer = true },
                    onSkipToNextClick = { miniPlayerViewModel.skipToNext() },
                    onSkipToPreviousClick = { miniPlayerViewModel.skipToPrevious() },
                    viewModel = miniPlayerViewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = showFullScreenPlayer,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.zIndex(1f)
        ) {
            currentSong?.let { song ->
                PlayerScreen(
                    song = song,
                    isPlaying = isPlaying,
                    onPlayPause = { miniPlayerViewModel.togglePlayPause() },
                    onClose = { showFullScreenPlayer = false },
                    onSkipToNextClick = { miniPlayerViewModel.skipToNext() },
                    onSkipToPreviousClick = { miniPlayerViewModel.skipToPrevious() },
                    viewModel = miniPlayerViewModel,
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
    viewModel: MiniPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val progress by viewModel.progress.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp)
    ) {
        ProgressSlider(
            progress = progress,
            onSeekTo = { viewModel.seekTo(it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        TimeIndicators(
            currentPosition = currentPosition,
            duration = duration
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPlayerClick() },
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
                Icon(Icons.Default.SkipPrevious, contentDescription = "Предыдущий трек")
            }

            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onSkipToNextClick) {
                Icon(Icons.Default.SkipNext, contentDescription = "Следующий трек")
            }
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
            Icon(Icons.Default.MoreVert, contentDescription = "Действия")
        }
    }
}

@Composable
fun AlbumCover(
    uri: Uri?,
    modifier: Modifier = Modifier.size(48.dp),
    shape: Shape = RectangleShape
) {
    uri?.let {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = modifier.clip(shape),
            contentScale = ContentScale.Crop
        )
    } ?: Icon(
        imageVector = Icons.Default.MusicNote,
        contentDescription = "Нет обложки"
    )
}

@Composable
fun ProgressSlider(progress: Float, onSeekTo: (Float) -> Unit) {
    Slider(
        value = progress,
        onValueChange = { onSeekTo(it.coerceIn(0f, 1f)) },
        modifier = Modifier.fillMaxWidth().height(4.dp),
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    )
}

@Composable
fun TimeIndicators(currentPosition: Long, duration: Long) {
    val positionText by remember(currentPosition) {
        derivedStateOf { currentPosition.toTimeString() }
    }
    val durationText by remember(duration) {
        derivedStateOf { duration.toTimeString() }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = positionText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = durationText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun Long.toTimeString(): String {
    if (this <= 0L) return "00:00"
    val seconds = (this / 1000) % 60
    val minutes = (this / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}


