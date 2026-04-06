package com.example.project_group12

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_group12.data.AppDao
import com.example.project_group12.data.AppDatabase
import com.example.project_group12.databinding.ActivityFavoriteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteBinding
    private lateinit var dao: AppDao
    private lateinit var adapter: SongAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dao = AppDatabase.getDatabase(this).appDao()

        binding.btnFavoriteBack.setOnClickListener { finish() }

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadFavoriteSongs()
    }

    private fun setupRecyclerView() {
        binding.rvFavoriteSongs.layoutManager = LinearLayoutManager(this)

        // Tái sử dụng SongAdapter của trang chủ
        adapter = SongAdapter(emptyList()) { clickedSong ->
            MusicPlayerManager.playSong(clickedSong) {}
            startActivity(Intent(this, PlayerActivity::class.java))
        }
        binding.rvFavoriteSongs.adapter = adapter
    }

    private fun loadFavoriteSongs() {
        lifecycleScope.launch(Dispatchers.IO) {
            val favSongs = dao.getFavoriteSongs()
            withContext(Dispatchers.Main) {
                adapter.updateData(favSongs)
            }
        }
    }
}