package com.timhome.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pot")
data class PotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val roomId: Int,
    val name: String,
    val channel: Int,
    // Mirrors the ESP32's per-pot "watering ineffective" alarm from the last poll.
    // Used to render the warning and to notify only on the false -> true transition.
    val alarmActive: Boolean = false,
)
