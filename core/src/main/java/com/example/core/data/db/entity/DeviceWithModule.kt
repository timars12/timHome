package com.example.core.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class DeviceWithModule(
    @Embedded val device: DeviceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "device_id"
    )
    val module: List<ModuleEntity>
)
