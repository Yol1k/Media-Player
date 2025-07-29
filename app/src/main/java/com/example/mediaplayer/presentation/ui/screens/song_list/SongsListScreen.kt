package com.example.mediaplayer.presentation.ui.screens.song_list

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.mediaplayer.domain.models.Song
import com.example.mediaplayer.presentation.ui.screens.player.PlayerScreen
import com.example.mediaplayer.presentation.ui.screens.player.PlayerViewModel
import com.example.mediaplayer.presentation.ui.theme.MediaPlayerTheme

@Composable
fun SongListScreen(
    songViewModel: SongListViewModel,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val songs by songViewModel.songs.collectAsState()
    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    
    var showFullScreenPlayer by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
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
                items(songs) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = currentSong?.id == song.id,
                        onPlayClick = { playerViewModel.playSong(song) },
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
                    onPlayPause = { playerViewModel.togglePlayPause() },
                    onPlayerClick = { showFullScreenPlayer = true },
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
                    onPlayPause = { playerViewModel.togglePlayPause() },
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
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.Gray.copy(alpha = 0.3f))
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

        IconButton(onClick = onPlayPause) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.primary
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
        Column(
            modifier = modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                fontWeight = FontWeight.Bold,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = song.artist,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onPlayClick, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Действия с треком"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SongListItemPreview() {
    MediaPlayerTheme {
        SongListItem(
            song = Song(id = 1, title = "Bohemian Rhapsody", artist = "Queen", path = ""),
            isPlaying = true,
            onPlayClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MiniPlayerPreview() {
    MediaPlayerTheme {
        MiniPlayer(
            song = Song(id = 1, title = "Bohemian Rhapsody", artist = "Queen", path = ""),
            isPlaying = true,
            onPlayPause = {},
            onPlayerClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}