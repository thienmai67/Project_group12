package com.example.project_group12

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_group12.data.AppDao
import com.example.project_group12.data.AppDatabase
import com.example.project_group12.databinding.ActivityMainBinding
import com.example.project_group12.repository.AuthRepository
import com.example.project_group12.repository.SongRepository
import com.example.project_group12.viewmodel.MainViewModel
import com.example.project_group12.viewmodel.MainViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: SongAdapter
    private lateinit var viewModel: MainViewModel
    private var rotateAnimation: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_disk)

        val dao = AppDatabase.getDatabase(this).appDao()
        val songRepository = SongRepository(dao)
        val authRepository = AuthRepository(dao)
        viewModel = ViewModelProvider(this, MainViewModelFactory(songRepository))[MainViewModel::class.java]

        setupRecyclerView()
        setupListeners(dao, authRepository)
        setupGenreFilters()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        updateMiniPlayer()
        viewModel.loadSongs()
    }

    private fun setupRecyclerView() {
        binding.rvHomeContent.layoutManager = LinearLayoutManager(this)
        adapter = SongAdapter(emptyList()) { clickedSong ->
            MusicPlayerManager.playSong(clickedSong) {
                updateMiniPlayer()
            }
            startActivity(Intent(this, PlayerActivity::class.java))
        }
        binding.rvHomeContent.adapter = adapter
    }

    private fun setupListeners(dao: AppDao, authRepository: AuthRepository) {
        lifecycleScope.launch(Dispatchers.IO) {
            val user = dao.getCurrentUser()
            val isGuest = (user?.role == "guest" || user == null)

            withContext(Dispatchers.Main) {
                if (isGuest) {
                    binding.layoutAuthButtons.visibility = View.VISIBLE
                    binding.tvGreeting.text = "Chào bạn"
                } else {
                    binding.layoutAuthButtons.visibility = View.GONE
                    binding.tvGreeting.text = if (user?.role == "admin") "Chào Quản trị viên" else "Chào ${user?.displayName}"
                }

                binding.btnProfile.setOnClickListener {
                    if (isGuest) {
                        Toast.makeText(this@MainActivity, "Vui lòng đăng nhập để xem hồ sơ!", Toast.LENGTH_SHORT).show()
                    } else {
                        showProfileMenu(it, authRepository, user?.role)
                    }
                }
            }
        }

        binding.btnHomeLogin.setOnClickListener { startActivity(Intent(this, LoginActivity::class.java)) }
        binding.btnHomeRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }

        binding.layoutMiniPlayer.setOnClickListener {
            if (MusicPlayerManager.currentSong != null) {
                startActivity(Intent(this, PlayerActivity::class.java))
            }
        }

        binding.btnMiniPlayPause.setOnClickListener {
            if (MusicPlayerManager.isPlaying) MusicPlayerManager.pause() else MusicPlayerManager.resume()
            updateMiniPlayer()
        }

        binding.btnSearchTop.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_shuffle -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val allSongs = dao.getAllSongs()
                        if (allSongs.isNotEmpty()) {
                            val randomSong = allSongs.random()
                            withContext(Dispatchers.Main) {
                                MusicPlayerManager.playSong(randomSong) { updateMiniPlayer() }
                                startActivity(Intent(this@MainActivity, PlayerActivity::class.java))
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "Chưa có bài hát nào để phát!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    false
                }
                R.id.nav_favorites -> {
                    startActivity(Intent(this@MainActivity, FavoriteActivity::class.java))
                    true
                }
                R.id.nav_download -> {
                    startActivity(Intent(this@MainActivity, DownloadActivity::class.java))
                    true
                }
                R.id.nav_home -> true
                else -> false
            }
        }
    }

    private fun setupGenreFilters() {
        binding.btnGenreAll.setOnClickListener { selectGenre("Tất cả", binding.btnGenreAll) }
        binding.btnGenrePop.setOnClickListener { selectGenre("Pop", binding.btnGenrePop) }
        binding.btnGenreRap.setOnClickListener { selectGenre("Rap", binding.btnGenreRap) }
        binding.btnGenreLofi.setOnClickListener { selectGenre("Lofi", binding.btnGenreLofi) }
    }

    private fun selectGenre(genre: String, selectedButton: Button) {
        viewModel.filterByGenre(genre)
        val defaultColor = ColorStateList.valueOf(Color.parseColor("#1AFFFFFF"))
        binding.btnGenreAll.backgroundTintList = defaultColor
        binding.btnGenrePop.backgroundTintList = defaultColor
        binding.btnGenreRap.backgroundTintList = defaultColor
        binding.btnGenreLofi.backgroundTintList = defaultColor

        selectedButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#00E5FF"))
    }

    private fun updateMiniPlayer() {
        val currentSong = MusicPlayerManager.currentSong
        if (currentSong != null) {
            binding.layoutMiniPlayer.visibility = View.VISIBLE
            binding.tvMiniTitle.text = currentSong.title
            binding.tvMiniArtist.text = currentSong.artist

            if (MusicPlayerManager.isPlaying) {
                binding.btnMiniPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                if (binding.imgMiniCover.animation == null) {
                    binding.imgMiniCover.startAnimation(rotateAnimation)
                }
            } else {
                binding.btnMiniPlayPause.setImageResource(android.R.drawable.ic_media_play)
                binding.imgMiniCover.clearAnimation()
            }
        } else {
            binding.layoutMiniPlayer.visibility = View.GONE
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.songs.collect { songList ->
                adapter.updateData(songList)
            }
        }
    }

    private fun showProfileMenu(view: View, authRepository: AuthRepository, role: String?) {
        val popup = PopupMenu(this, view)
        if (role == "admin") popup.menu.add("Quản lý nhạc (Admin)")
        popup.menu.add("Đăng xuất")
        popup.setOnMenuItemClickListener {
            when (it.title) {
                "Quản lý nhạc (Admin)" -> {
                    startActivity(Intent(this@MainActivity, AdminActivity::class.java))
                    true
                }
                "Đăng xuất" -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        MusicPlayerManager.stop()
                        authRepository.logout()
                        withContext(Dispatchers.Main) {
                            val intent = Intent(this@MainActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}