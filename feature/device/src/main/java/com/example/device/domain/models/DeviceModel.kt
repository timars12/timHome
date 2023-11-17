package com.example.device.domain.models

import com.example.device.data.model.ModuleModel

internal data class DeviceModel(
    val id: Int,
    val image: String? = null,
    val title: String,
    val description: String? = null,
    val totalPrice: String,
    val rating: Float = 0f,
    val isFavorite: Boolean = false,
    val dateCreated: String
)

internal data class DeviceWithModuleModel(val device: DeviceModel, val modules: List<ModuleModel>)
