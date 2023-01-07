package com.example.device.data.mappers

import com.example.core.data.db.entity.DeviceEntity
import com.example.device.domain.models.DeviceModel
import javax.inject.Inject

class DeviceMapper @Inject constructor() {
    fun convertToEntityModel(model: DeviceModel): DeviceEntity {
        return DeviceEntity(
            id = model.id,
            image = model.image,
            title = model.title,
            description = model.description,
            totalPrice = model.totalPrice,
            rating = model.rating,
            isFavorite = model.isFavorite,
            dateCreated = model.dateCreated
        )
    }

    fun convertEntityToModel(entity: DeviceEntity): DeviceModel {
        return DeviceModel(
            id = entity.id,
            image = entity.image,
            title = entity.title,
            description = entity.description,
            totalPrice = entity.totalPrice,
            rating = entity.rating,
            isFavorite = entity.isFavorite,
            dateCreated = entity.dateCreated
        )
    }
}
