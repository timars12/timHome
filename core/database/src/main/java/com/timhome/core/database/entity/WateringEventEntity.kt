package com.timhome.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Records a manual watering so the UI can show "last watered at ...".
 * Whether the watering was effective is decided on the ESP32 and mirrored
 * onto [PotEntity.alarmActive] — it is deliberately not tracked here anymore.
 */
@Entity(tableName = "watering_event")
data class WateringEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val potId: Int,
    val wateredAt: String,
)
