package com.example.project_group12.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserLocal(
    @PrimaryKey
    val uid: String,             // ID do Firebase Auth cấp
    val email: String,
    val role: String,           // Sẽ là "admin", "user", hoặc "guest"
    val displayName: String,
)