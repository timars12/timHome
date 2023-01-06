package com.example.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity (
    @PrimaryKey
    val id: Int,
    val image: String?,
    val title: String,
    val description: String?,
    val totalPrice: String,
    val rating: Float = 0f,
    val isFavorite: Boolean,
    val dateCreated: String
)