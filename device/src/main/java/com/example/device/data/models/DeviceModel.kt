package com.example.device.data.models

import com.example.core.data.db.entity.DeviceEntity

data class DeviceModel(
    val id: Int,
    val image: String? = null,
    val title: String,
    val description: String? = null,
    val totalPrice: String,
    val rating: Float = 0f,
    val isFavorite: Boolean = false,
    val dateCreated: String
) {
    fun convertToEntityModel(): DeviceEntity {
        return DeviceEntity(
            id = id,
            image = image,
            title = title,
            description = description,
            totalPrice = totalPrice,
            rating = rating,
            isFavorite = isFavorite,
            dateCreated = dateCreated
        )
    }
}
