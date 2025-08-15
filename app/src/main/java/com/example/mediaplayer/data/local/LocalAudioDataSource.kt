package com.example.mediaplayer.data.local

import android.content.Context
import android.provider.MediaStore
import com.example.mediaplayer.domain.models.Song

class LocalAudioDataSource(private val context: Context) {

    fun getLocalSongs(): List<Song> {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM
        )

        return context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val albumIndex= cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)

            buildList {
                while (cursor.moveToNext()) {
                    add(
                        Song(
                            id = cursor.getLong(idIndex),
                            title = cursor.getString(titleIndex) ?: "Неизвестный трек",
                            artist = cursor.getString(artistIndex) ?: "Неизвестный исполнитель",
                            path = cursor.getString(pathIndex) ?: "",
                            albumId = cursor.getLong(albumIdIndex),
                            album = cursor.getString(albumIndex) ?: "Неизвестный альбом"
                        )
                    )
                }
            }
        } ?: emptyList()
    }
}