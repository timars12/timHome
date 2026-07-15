package com.timhome.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carbon_dioxide")
data class CarbonDioxideEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val co2Level: Int,
    val date: String,
)
