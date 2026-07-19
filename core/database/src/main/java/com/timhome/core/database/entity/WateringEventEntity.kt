package com.timhome.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watering_event")
data class WateringEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val potId: Int,
    val wateredAt: String,
    val moistureBeforeWatering: Int,
    val isEffective: Boolean? = null,
)
