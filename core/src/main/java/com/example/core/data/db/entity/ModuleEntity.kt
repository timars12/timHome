package com.example.core.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "modules",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            childColumns = ["device_id"],
            parentColumns = ["id"],
            onDelete = CASCADE,
        ),
    ],
    indices = [Index(value = ["device_id"])],
)
data class ModuleEntity(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "device_id") val deviceId: Int,
    val image: String?,
    val title: String,
    val price: String,
    val link: String,
)
