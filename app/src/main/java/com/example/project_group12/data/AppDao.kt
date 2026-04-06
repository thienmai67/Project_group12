package com.example.project_group12.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AppDao {
    // --- Thao tác với Bài hát (CRUD) ---
    @Query("SELECT * FROM songs")
    fun getAllSongs(): List<Song>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :searchQuery || '%'")
    fun searchSongs(searchQuery: String): List<Song>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSong(song: Song)

    @Update
    fun updateSong(song: Song)

    @Delete
    fun deleteSong(song: Song)

    // --- Thao tác với Người dùng ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveUser(user: UserLocal)

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getCurrentUser(): UserLocal?

    @Query("DELETE FROM user_profile")
    fun clearUser()

    // --- Thao tác với Nhạc Đã Tải ---
    @Query("SELECT * FROM downloaded_songs")
    fun getDownloadedSongs(): List<DownloadedSong>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDownloadedSong(song: DownloadedSong)

    @Delete
    fun deleteDownloadedSong(song: DownloadedSong)
    // --- Thao tác với Yêu thích ---
    @Query("SELECT * FROM songs WHERE isFavorite = 1")
    fun getFavoriteSongs(): List<Song>

    @Query("UPDATE songs SET isFavorite = :isFav WHERE id = :songId")
    fun updateFavoriteStatus(songId: String, isFav: Boolean)
}