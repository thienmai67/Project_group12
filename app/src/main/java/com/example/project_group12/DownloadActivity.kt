package com.example.project_group12

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_group12.data.AppDao
import com.example.project_group12.data.AppDatabase
import com.example.project_group12.data.Song
import com.example.project_group12.databinding.ActivityDownloadBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DownloadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDownloadBinding
    private lateinit var dao: AppDao
    private lateinit var adapter: AdminSongAdapter // Tái sử dụng lại AdminSongAdapter vì nó có nút Xóa

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dao = AppDatabase.getDatabase(this).appDao()
        binding.btnDownloadBack.setOnClickListener {
            finish()
        }

        setupRecyclerView()
        loadDownloadedSongs()
    }

    private fun setupRecyclerView() {
        binding.rvDownloadedSongs.layoutManager = LinearLayoutManager(this)

        adapter = AdminSongAdapter(
            songs = emptyList(),
            onEditClick = { song ->
                // Nút sửa (bỏ qua, thay bằng nút play)
                playLocalSong(song)
            },
            onDeleteClick = { song ->
                deleteDownloadedSong(song)
            }
        )
        binding.rvDownloadedSongs.adapter = adapter

    }

    private fun loadDownloadedSongs() {
        lifecycleScope.launch(Dispatchers.IO) {
            val downloadedList = dao.getDownloadedSongs()

            // Chuyển đổi từ DownloadedSong sang Song để dùng chung Adapter
            val songList = downloadedList.map {
                Song(it.id, it.title, it.artist, it.coverUrl, it.localPath, "Downloaded")
            }

            withContext(Dispatchers.Main) {
                adapter.updateData(songList)
            }
        }
    }

    private fun playLocalSong(song: Song) {
        MusicPlayerManager.playSong(song) {}
        startActivity(Intent(this, PlayerActivity::class.java))
    }

    private fun deleteDownloadedSong(song: Song) {
        lifecycleScope.launch(Dispatchers.IO) {
            // 1. Xóa file vật lý trên điện thoại
            val file = File(song.mp3Url)
            if (file.exists()) {
                file.delete()
            }

            // 2. Xóa khỏi Database
            val downloadedList = dao.getDownloadedSongs()
            val target = downloadedList.find { it.id == song.id }
            if (target != null) {
                dao.deleteDownloadedSong(target)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@DownloadActivity, "Đã xóa bài hát", Toast.LENGTH_SHORT).show()
                loadDownloadedSongs() // Cập nhật lại UI
            }
        }
    }
}