package com.example.device.data.models

data class DeviceModel(
    val id: Int,
    val image: String? = null,
    val title: String,
    val description: String? = null,
    val totalPrice: String,
    val rating: Float = 0f,
    val isFavorite: Boolean = false,
    val dateCreated: String
)
