package com.timhome.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "soil_moisture_reading")
data class SoilMoistureReadingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val potId: Int,
    val moisturePercent: Int,
)
