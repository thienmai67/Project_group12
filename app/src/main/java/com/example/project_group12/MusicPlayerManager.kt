package com.example.project_group12

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.project_group12.data.Song

object MusicPlayerManager {
    var mediaPlayer: MediaPlayer? = null
    var currentSong: Song? = null
    var isPlaying = false

    fun playSong(song: Song, onPrepared: () -> Unit) {
        if (currentSong?.id == song.id && mediaPlayer != null) {
            resume()
            onPrepared()
            return
        }

        currentSong = song
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            try {
                setDataSource(song.mp3Url)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    // ĐÃ SỬA LỖI: Chỉ đích danh biến của MusicPlayerManager
                    MusicPlayerManager.isPlaying = true
                    onPrepared()
                }
                setOnCompletionListener {
                    // ĐÃ SỬA LỖI: Chỉ đích danh biến của MusicPlayerManager
                    MusicPlayerManager.isPlaying = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        isPlaying = false
    }

    fun resume() {
        mediaPlayer?.start()
        isPlaying = true
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0
    fun getDuration(): Int = mediaPlayer?.duration ?: 0
}