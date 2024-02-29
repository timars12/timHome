package com.example.device.data.model

internal data class Device(
    val id: Int,
    val image: String? = null,
    val ownerName: String,
    val title: String,
    val description: String? = null,
    val totalPrice: String,
    val rating: Float = 0f,
    val isFavorite: Boolean = false,
    val dateCreated: String,
    val modules: List<ModuleModel>,
)

internal data class ModuleModel(
    val id: Int,
    val image: String? = null,
    val title: String,
    val description: String? = null,
    val price: String,
    val link: String,
)
