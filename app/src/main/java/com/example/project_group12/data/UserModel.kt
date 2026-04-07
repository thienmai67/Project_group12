package com.example.project_group12.data

data class UserModel(
    val uid: String,
    val username: String,
    val displayName: String,
    var role: String // "admin" hoặc "user"
)