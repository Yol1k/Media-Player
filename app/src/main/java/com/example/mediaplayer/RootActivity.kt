package com.example.mediaplayer

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.mediaplayer.data.local.LocalAudioDataSource
import com.example.mediaplayer.presentation.ui.screens.player.PlayerViewModel
import com.example.mediaplayer.presentation.ui.screens.song_list.SongListScreen
import com.example.mediaplayer.presentation.ui.screens.song_list.SongListViewModel
import com.example.mediaplayer.presentation.ui.theme.MediaPlayerTheme

class RootActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            initApp()
        } else {
            showPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            READ_MEDIA_AUDIO
        } else {
            READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            initApp()
        } else {
            permissionLauncher.launch(permission)
        }
    }

    private fun initApp() {
        setContent {
            MediaPlayerTheme {
                val songViewModel = SongListViewModel(LocalAudioDataSource(this))
                val playerViewModel = PlayerViewModel(application)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SongListScreen(
                        songViewModel = songViewModel,
                        playerViewModel = playerViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun showPermissionDenied() {
        setContent {
            MediaPlayerTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Приложению нужен доступ к аудио для работы")
                }
            }
        }
    }
}


