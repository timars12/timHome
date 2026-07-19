package com.timhome.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val ipAddress: String,
    val lastPollSuccessful: Boolean = true,
    val lastPollError: String? = null,
)
