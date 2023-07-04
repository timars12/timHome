package com.example.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "userEntity")
data class UserEntity(
    @PrimaryKey val id: Int,
    val email: String,
    val userName: String,
    val avatar: String?
)
