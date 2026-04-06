package com.example.project_group12

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation
import com.example.project_group12.data.AppDatabase
import com.example.project_group12.data.DownloadedSong
import com.example.project_group12.databinding.ActivityPlayerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var isGuest = true
    private val handler = Handler(Looper.getMainLooper())
    private var isUserSeeking = false
    private var rotateAnimation: Animation? = null

    private val updateSeekbarRunnable = object : Runnable {
        override fun run() {
            if (!isUserSeeking) {
                try {
                    val duration = MusicPlayerManager.getDuration()
                    if (duration > 0 && binding.seekBarPlayer.max != duration) {
                        binding.seekBarPlayer.max = duration
                        binding.tvTotalTime.text = formatTime(duration)
                    }

                    val currentPos = MusicPlayerManager.getCurrentPosition()
                    binding.seekBarPlayer.progress = currentPos
                    binding.tvCurrentTime.text = formatTime(currentPos)

                    if (isGuest && currentPos >= 30000 && MusicPlayerManager.isPlaying) {
                        MusicPlayerManager.pause()
                        updatePlayPauseStatus()
                        Toast.makeText(this@PlayerActivity, "Tài khoản Khách chỉ được nghe 30s!", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo animation
        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_disk)

        val dao = AppDatabase.getDatabase(this).appDao()

        lifecycleScope.launch(Dispatchers.IO) {
            val user = dao.getCurrentUser()
            isGuest = (user?.role == "guest" || user == null)
        }

        val currentSong = MusicPlayerManager.currentSong
        if (currentSong != null) {
            binding.tvPlayerTitle.text = currentSong.title
            binding.tvPlayerArtist.text = currentSong.artist

            // 1. Tải ảnh vào đĩa nhạc (Code cũ)
            Glide.with(this)
                .load(currentSong.coverUrl)
                .placeholder(android.R.drawable.ic_media_play)
                .into(binding.imgPlayerCover)

            // 2. PHÉP THUẬT: Tải ảnh làm Nền động (Blur)
            Glide.with(this)
                .load(currentSong.coverUrl)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                .into(binding.imgBlurredBackground)

            try {
                val duration = MusicPlayerManager.getDuration()
                binding.seekBarPlayer.max = duration
                binding.tvTotalTime.text = formatTime(duration)
            } catch (e: Exception) {
                binding.seekBarPlayer.max = 0
            }
        }

        updatePlayPauseStatus()
        handler.postDelayed(updateSeekbarRunnable, 0)
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnPlayerBack.setOnClickListener { finish() }

        binding.btnPlayerLike.setOnClickListener {
            if (isGuest) {
                Toast.makeText(this, "Đăng nhập để thêm vào Yêu thích!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val song = MusicPlayerManager.currentSong ?: return@setOnClickListener
            val newFavStatus = !song.isFavorite
            MusicPlayerManager.currentSong = song.copy(isFavorite = newFavStatus)

            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getDatabase(this@PlayerActivity).appDao()
                dao.updateFavoriteStatus(song.id, newFavStatus)

                runOnUiThread {
                    updateLikeButtonIcon(newFavStatus)
                    val msg = if (newFavStatus) "Đã thêm vào Yêu thích" else "Đã bỏ Yêu thích"

                    // --- ĐẶT SNACKBAR Ở ĐÂY ĐỂ HIỆN KHI THÀNH CÔNG ---
                    val snackbar = com.google.android.material.snackbar.Snackbar.make(binding.root, msg, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                    snackbar.setBackgroundTint(android.graphics.Color.parseColor("#222222")) // Nền đen kính
                    snackbar.setTextColor(android.graphics.Color.parseColor("#00E5FF")) // Chữ xanh dạ quang
                    snackbar.show()
                }
            }
        }

        binding.seekBarPlayer.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) binding.tvCurrentTime.text = formatTime(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val newPosition = seekBar?.progress ?: 0
                if (isGuest && newPosition >= 30000) {
                    MusicPlayerManager.seekTo(29000)
                    binding.seekBarPlayer.progress = 29000
                    binding.tvCurrentTime.text = formatTime(29000)
                } else {
                    MusicPlayerManager.seekTo(newPosition)
                    binding.tvCurrentTime.text = formatTime(newPosition)
                }

                handler.postDelayed({
                    isUserSeeking = false
                }, 800)
            }
        })

        binding.btnPlayerLyrics.setOnClickListener {
            if (isGuest) {
                Toast.makeText(this, "Đăng nhập để xem lời!", Toast.LENGTH_LONG).show()
            } else {
                AlertDialog.Builder(this).setTitle("Lời bài hát").setMessage("Đang cập nhật...").show()
            }
        }

        updateLikeButtonIcon(MusicPlayerManager.currentSong?.isFavorite == true)

        binding.btnPlayerLike.setOnClickListener {
            if (isGuest) {
                Toast.makeText(this, "Đăng nhập để thêm vào Yêu thích!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val song = MusicPlayerManager.currentSong ?: return@setOnClickListener
            val newFavStatus = !song.isFavorite
            MusicPlayerManager.currentSong = song.copy(isFavorite = newFavStatus)

            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getDatabase(this@PlayerActivity).appDao()
                dao.updateFavoriteStatus(song.id, newFavStatus)

                runOnUiThread {
                    updateLikeButtonIcon(newFavStatus)
                    val msg = if (newFavStatus) "Đã thêm vào Yêu thích" else "Đã bỏ Yêu thích"
                    Toast.makeText(this@PlayerActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnPlayerDownload.setOnClickListener {
            if (isGuest) {
                Toast.makeText(this, "Vui lòng đăng nhập để tải bài hát!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val currentSong = MusicPlayerManager.currentSong
            if (currentSong == null) return@setOnClickListener

            Toast.makeText(this, "Đang tải bài hát: ${currentSong.title}...", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val url = URL(currentSong.mp3Url)
                    val fileName = "${currentSong.id}.mp3"
                    val file = File(this@PlayerActivity.filesDir, fileName)

                    url.openStream().use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }

                    val dao = AppDatabase.getDatabase(this@PlayerActivity).appDao()
                    val downloadedSong = DownloadedSong(
                        id = currentSong.id,
                        title = currentSong.title,
                        artist = currentSong.artist,
                        coverUrl = currentSong.coverUrl,
                        localPath = file.absolutePath
                    )
                    dao.insertDownloadedSong(downloadedSong)

                    runOnUiThread {
                        Toast.makeText(this@PlayerActivity, "Tải thành công: ${currentSong.title}!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this@PlayerActivity, "Lỗi khi tải bài hát!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updatePlayPauseStatus() {
        if (MusicPlayerManager.isPlaying) {
            binding.btnPlayerPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            startRotateDisk()
        } else {
            binding.btnPlayerPlayPause.setImageResource(android.R.drawable.ic_media_play)
            stopRotateDisk()
        }
    }

    private fun startRotateDisk() {
        if (binding.cardPlayerCover.animation == null) {
            binding.cardPlayerCover.startAnimation(rotateAnimation)
        }
    }

    private fun stopRotateDisk() {
        binding.cardPlayerCover.clearAnimation()
    }

    private fun updateLikeButtonIcon(isFavorite: Boolean) {
        if (isFavorite) {
            binding.btnPlayerLike.setImageResource(R.drawable.ic_heart_filled)
            binding.btnPlayerLike.imageTintList = ColorStateList.valueOf(Color.parseColor("#FF007F")) // Tim hồng dạ quang
        } else {
            binding.btnPlayerLike.setImageResource(R.drawable.ic_heart_outline)
            binding.btnPlayerLike.imageTintList = ColorStateList.valueOf(Color.WHITE) // Tim rỗng màu trắng
        }
    }

    private fun formatTime(millis: Int): String {
        val totalSeconds = millis / 1000
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60)
    }

    override fun onResume() {
        super.onResume()
        updatePlayPauseStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSeekbarRunnable)
    }
}