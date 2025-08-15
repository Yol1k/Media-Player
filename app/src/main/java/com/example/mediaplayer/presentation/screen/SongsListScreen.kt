package com.example.mediaplayer.presentation.screen

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.media3.common.util.UnstableApi
import com.example.mediaplayer.R
import com.example.mediaplayer.domain.models.Song
import com.example.mediaplayer.presentation.viewmodel.MiniPlayerViewModel
import com.example.mediaplayer.presentation.viewmodel.RepeatMode
import com.example.mediaplayer.presentation.viewmodel.SongListViewModel
import java.util.Locale

@OptIn(UnstableApi::class)
@Composable
fun SongListScreen(
    modifier: Modifier = Modifier,
    songViewModel: SongListViewModel,
    miniPlayerViewModel: MiniPlayerViewModel,
) {
    val songs by songViewModel.songs.collectAsState()
    val playerState by miniPlayerViewModel.playerState.collectAsState()
    var showFullScreenPlayer by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        songViewModel.loadSongs()
    }

    LaunchedEffect(songs) {
        if (songs.isNotEmpty() && playerState.currentSong == null) {
            miniPlayerViewModel.setPlaylist(songs)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        SongList(
            songs = songs,
            currentSong = playerState.currentSong,
            onPlayClick = { song ->
                if (playerState.currentSong?.id == song.id) {
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
            playerState.currentSong?.let { song ->
                PlayerScreen(
                    song = song,
                    onPlayPause = { miniPlayerViewModel.togglePlayPause() },
                    onClose = { showFullScreenPlayer = false },
                    onSkipToNextClick = { miniPlayerViewModel.skipToNext() },
                    onSkipToPreviousClick = { miniPlayerViewModel.skipToPrevious() },
                    viewModel = miniPlayerViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        playerState.currentSong?.let { song ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
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
            .padding(bottom = if (currentSong != null && !showFullScreenPlayer) 100.dp else 0.dp),
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
    val icon = remember { R.drawable.more_vert }
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
            albumId = song.albumId,
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

        IconButton(onClick = { }) {
            Icon(painter = painterResource(id = icon), contentDescription = "Действия")
        }
    }
}

@Composable
fun AlbumCover(
    modifier: Modifier = Modifier,
    albumId: Long,
    shape: Shape = RectangleShape
) {
    val context = LocalContext.current
    val bitmap = remember(albumId) {
        if (albumId > 0) {
            try {
                val uri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Обложка альбома",
            modifier = modifier
                .size(48.dp)
                .clip(shape),
            contentScale = ContentScale.Crop
        )
    } else {
        Icon(
            painter = painterResource(id = R.drawable.music_note),
            contentDescription = "Нет обложки",
            modifier = modifier.size(48.dp)
        )
    }
}

@OptIn(UnstableApi::class)
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

    val playerState by viewModel.playerState.collectAsState()

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onPlayerClick() }
                .padding(vertical = 16.dp)
        ) {
            AlbumCover(
                albumId = song.albumId,
                modifier = Modifier
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

        ProgressSlider(
            progress = playerState.progress,
            onSeekTo = { viewModel.seekTo(it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        TimeIndicators(
            currentPosition = playerState.currentPosition,
            duration = playerState.duration
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.toggleRepeatMode() }) {
                Icon(
                    painter = painterResource(
                        id = when (playerState.repeatMode) {
                            RepeatMode.NONE -> R.drawable.repeat
                            RepeatMode.ALL -> R.drawable.repeat_on
                            RepeatMode.ONE -> R.drawable.repeat_one
                        }
                    ),
                    contentDescription = "Режим повтора",
                    tint = when (playerState.repeatMode) {
                        RepeatMode.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }

            IconButton(onClick = onSkipToPreviousClick) {
                Icon(painter = painterResource(id = R.drawable.skip_previous), contentDescription = "Предыдущий трек")
            }

            IconButton(onClick = onPlayPause) {
                Icon(
                    painter = painterResource(id = if (playerState.isPlaying) R.drawable.pause else R.drawable.play_arrow),
                    contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
            }

            IconButton(onClick = onSkipToNextClick) {
                Icon(painter = painterResource(id = R.drawable.skip_next), contentDescription = "Следующий трек")
            }

            IconButton(onClick = { }) {
                Icon(painter = painterResource(id = R.drawable.more_vert), contentDescription = "Действия")
            }
        }
    }
}

@Composable
fun ProgressSlider(
    progress: Float,
    onSeekTo: (Float) -> Unit
)
{
    var isSeeking by remember { mutableStateOf(false) }
    var localProgress by remember { mutableFloatStateOf(progress) }
    Slider(
        value = if (isSeeking) localProgress else progress,
        onValueChange = {
            isSeeking = true
            localProgress = it.coerceIn(0f, 1f)
        },
        onValueChangeFinished = {
            onSeekTo(localProgress)
            isSeeking = false
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp),
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


