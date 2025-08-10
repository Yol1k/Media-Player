package com.example.mediaplayer.presentation.screen

import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediaplayer.domain.models.Song

@Composable
fun PlayerScreen(
    song: Song,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSkipToNextClick: () -> Unit,
    onSkipToPreviousClick: () -> Unit,
    onClose: () -> Unit,
    progress: Float,
    currentPosition: Long,
    duration: Long,
    onSeekTo: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.surface
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        // Верхняя панель с кнопкой закрытия
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Закрыть",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            IconButton(onClick = { /* TODO: Добавить меню */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Меню",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Основной контент
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            AlbumCover(
                uri = song.cover,
                modifier = Modifier.size(300.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Информация о песне
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (song.album != "Неизвестно") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.album,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Slider(
                value = progress,
                onValueChange = { newProgress ->
                    onSeekTo(newProgress.coerceIn(0f, 1f))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.Transparent,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentPosition.toTimeString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = duration.toTimeString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Кнопки управления
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Кнопка предыдущего трека
                IconButton(
                    onClick = onSkipToPreviousClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Предыдущий",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }


                // Главная кнопка воспроизведения/паузы
                FloatingActionButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(72.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Пауза" else "Воспроизвести",
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                // Кнопка следующего трека
                IconButton(
                    onClick = onSkipToNextClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Переключить на следующий трек",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Дополнительные кнопки
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { /* TODO: Режим повтора */ }) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Повтор",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = { /* TODO: Перемешать */ }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Перемешать",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = { /* TODO: Добавить в плейлист */ }) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = "Добавить в плейлист",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = { /* TODO: Поделиться */ }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Поделиться",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerScreenPlayingPreview() {
    MaterialTheme {
        PlayerScreen(
            song = Song(
                id = 1,
                title = "Bohemian Rhapsody (Live at Wembley 1986)",
                artist = "Queen",
                album = "A Night at the Opera",
                path = "/storage/emulated/0/Music/queen.mp3",
                albumId = 123,
                cover = Uri.parse("content://media/external/audio/albumart/123")
            ),
            isPlaying = true,
            onPlayPause = {},
            onSkipToNextClick = {},
            onSkipToPreviousClick = {},
            onClose = {},
            onSeekTo = {},
            duration = 100000,
            currentPosition = 1000,
            progress = 10.0f
        )
    }
}

private fun Long.toTimeString(): String {
    if (this <= 0L) return "00:00"
    val seconds = (this / 1000) % 60
    val minutes = (this / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
