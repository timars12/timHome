package com.example.device.domain.models

import androidx.annotation.DrawableRes

internal data class DeviceModel(
    val id: Int,
    @DrawableRes val image: Int,
    val title: String,
    val description: String? = null,
    val totalPrice: String,
    val rating: Float = 0f,
    val isFavorite: Boolean = false,
    val dateCreated: String,
)
