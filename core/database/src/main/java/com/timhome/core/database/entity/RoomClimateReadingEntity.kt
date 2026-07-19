package com.timhome.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_climate_reading")
data class RoomClimateReadingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val roomId: Int,
    val temperature: Double,
    val humidity: Double,
    val timestamp: String,
)
