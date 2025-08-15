package com.example.mediaplayer.presentation.screen

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.filled.RepeatOneOn
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.mediaplayer.domain.models.Song
import com.example.mediaplayer.presentation.viewmodel.MiniPlayerViewModel
import com.example.mediaplayer.presentation.viewmodel.RepeatMode
import com.example.mediaplayer.presentation.viewmodel.SongListViewModel
import java.util.Locale

@Composable
fun SongListScreen(
    modifier: Modifier = Modifier,
    songViewModel: SongListViewModel,
    miniPlayerViewModel: MiniPlayerViewModel,
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
        SongList(
            songs = songs,
            currentSong = currentSong,
            onPlayClick = { song ->
                if (currentSong?.id == song.id) {
                    miniPlayerViewModel.togglePlayPause()
                } else {
                    miniPlayerViewModel.playSong(song)
                }
            },
            showFullScreenPlayer = showFullScreenPlayer
        )

        AnimatedVisibility(
            visible = showFullScreenPlayer,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
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

        currentSong?.let { song ->
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            ) {
                MiniPlayer(
                    song = song,
                    onPlayPause = { miniPlayerViewModel.togglePlayPause() },
                    onPlayerClick = { showFullScreenPlayer = true },
                    onSkipToNextClick = { miniPlayerViewModel.skipToNext() },
                    onSkipToPreviousClick = { miniPlayerViewModel.skipToPrevious() },
                    viewModel = miniPlayerViewModel,
                )
            }
        }
    }
}

@Composable
private fun SongList(
    songs: List<Song>,
    currentSong: Song?,
    onPlayClick: (Song) -> Unit,
    showFullScreenPlayer: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = if (currentSong != null && !showFullScreenPlayer) 72.dp else 0.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = songs,
            key = { it.id },
            contentType = { "song_item" }
        ) { song ->
            SongListItem(
                song = song,
                isPlaying = currentSong?.id == song.id,
                onPlayClick = { onPlayClick(song) }
            )
        }
    }
}

@Composable
private fun SongListItem(
    modifier: Modifier = Modifier,
    song: Song,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onPlayClick
            )
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
    modifier: Modifier = Modifier,
    uri: Uri?,
    shape: Shape = RectangleShape
) {
    uri?.let {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = modifier.size(48.dp).clip(shape),
            contentScale = ContentScale.Crop
        )
    } ?: Icon(
        imageVector = Icons.Default.MusicNote,
        contentDescription = "Нет обложки"
    )
}

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    song: Song,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onSkipToNextClick: () -> Unit,
    onSkipToPreviousClick: () -> Unit,
    viewModel: MiniPlayerViewModel,
) {
    val progress by viewModel.progress.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp)
    ) {
        ProgressSlider(
            progress = progress,
            onSeekTo = { viewModel.seekTo(it) },
            isPlaying = isPlaying,
            viewModel = viewModel
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

            IconButton(onClick = { viewModel.toggleRepeatMode() }) {
                Icon(
                    imageVector = when (repeatMode) {
                        RepeatMode.NONE -> Icons.Default.Repeat
                        RepeatMode.ALL -> Icons.Default.RepeatOn
                        RepeatMode.ONE -> Icons.Default.RepeatOneOn
                    },
                    contentDescription = "Режим повтора",
                    tint = when (repeatMode) {
                        RepeatMode.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
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
fun ProgressSlider(
    progress: Float,
    onSeekTo: (Float) -> Unit,
    isPlaying: Boolean,
    viewModel: MiniPlayerViewModel)
{
    var isSeeking by remember { mutableStateOf(false) }
    var localProgress by remember { mutableFloatStateOf(0f) }
    val shownProgress by remember(isSeeking, progress, localProgress) {
        derivedStateOf { if (isSeeking) localProgress else progress }
    }
    Slider(
        value = shownProgress,
        onValueChange = {
            isSeeking = true
            localProgress = it.coerceIn(0f, 1f)
            viewModel.seekTo(it)
        },
        onValueChangeFinished = {
            onSeekTo(localProgress)
            if (!isPlaying) {
                viewModel.togglePlayPause()
            }
            isSeeking = false
        },
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
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}


