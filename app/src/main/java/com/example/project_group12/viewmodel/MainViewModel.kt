package com.example.project_group12.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.project_group12.data.Song
import com.example.project_group12.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: SongRepository) : ViewModel() {
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs

    // Thêm một biến backup để lưu toàn bộ bài hát gốc
    private var allSongsBackup: List<Song> = emptyList()

    init {
        loadSongs()
    }

    fun loadSongs() {
        viewModelScope.launch {
            // Lấy từ Local trước
            val localSongs = repository.getLocalSongs()
            allSongsBackup = localSongs
            _songs.value = localSongs

            // Lấy từ Firebase cập nhật vào
            val result = repository.syncSongsFromFirebase()
            result.onSuccess { newSongs ->
                allSongsBackup = newSongs
                _songs.value = newSongs
            }
        }
    }

    // THÊM MỚI: Hàm xử lý lọc bài hát theo thể loại
    fun filterByGenre(genre: String) {
        if (genre == "Tất cả") {
            _songs.value = allSongsBackup
        } else {
            // Lọc ra các bài hát có thể loại chứa chuỗi (không phân biệt hoa thường)
            _songs.value = allSongsBackup.filter { song ->
                song.genre.contains(genre, ignoreCase = true)
            }
        }
    }
}
class MainViewModelFactory(private val repository: SongRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}