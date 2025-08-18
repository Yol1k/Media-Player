package com.example.mediaplayer.presentation.screen

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.example.mediaplayer.R
import com.example.mediaplayer.domain.models.Song
import com.example.mediaplayer.presentation.viewmodel.MiniPlayerViewModel
import com.example.mediaplayer.presentation.viewmodel.RepeatMode

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    song: Song,
    onPlayPause: () -> Unit,
    onClose: () -> Unit,
    onSkipToNextClick: () -> Unit,
    onSkipToPreviousClick: () -> Unit,
    viewModel: MiniPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val playerState by viewModel.playerState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { }),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onClose) {
                Icon(painter = painterResource(id = R.drawable.close), contentDescription = "Закрыть")
            }
        }

        AlbumCover(
            albumId = song.albumId,
            modifier = Modifier.size(400.dp).padding(16.dp),
            shape = CircleShape
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = song.title,
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column (
            modifier = Modifier.padding(16.dp)
        ) {
            ProgressSlider(
                progress = playerState.progress,
                onSeekTo = { viewModel.seekTo(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TimeIndicators(
                currentPosition = playerState.currentPosition,
                duration = playerState.duration
            )

        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                Icon(
                    painter = painterResource(id = R.drawable.skip_previous),
                    contentDescription = "Предыдущий трек",
                    modifier = Modifier.size(50.dp))
            }

            IconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    painter = painterResource(id = if (playerState.isPlaying) R.drawable.pause else R.drawable.play_arrow),
                    contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
            }

            IconButton(onClick = onSkipToNextClick) {
                Icon(
                    painter = painterResource(id = R.drawable.skip_next),
                    contentDescription = "Следующий трек",
                    modifier = Modifier.size(50.dp)
                )
            }

            IconButton(onClick = {}) {
                Icon(painter = painterResource(id = R.drawable.equalizer), contentDescription = "Эквалайзер")
            }
        }
    }
}

