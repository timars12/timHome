package com.example.device.data.mappers

import com.example.core.data.db.entity.DeviceEntity
import com.example.core.data.db.entity.ModuleEntity
import com.example.core.di.scope.FeatureScope
import com.example.device.data.model.Device
import com.example.device.data.model.ModuleModel
import com.example.device.domain.models.DeviceModel
import javax.inject.Inject

@FeatureScope
internal class DeviceMapper @Inject constructor() {
    fun convertToEntityModel(device: Device): DeviceEntity {
        return DeviceEntity(
            id = device.id,
            image = device.image,
            ownerName = device.ownerName,
            title = device.title,
            description = device.description,
            totalPrice = device.totalPrice,
            rating = device.rating,
            isFavorite = device.isFavorite,
            dateCreated = device.dateCreated
        )
    }

    fun convertModuleToModuleEntity(deviceId: Int, model: ModuleModel): ModuleEntity {
        return ModuleEntity(
            id = model.id,
            deviceId = deviceId,
            image = model.image,
            title = model.title,
            price = model.price,
            link = model.link
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

    fun convertModuleEntityToModel(entity: ModuleEntity): ModuleModel {
        return ModuleModel(
            id = entity.id,
            image = entity.image,
            title = entity.title,
            price = entity.price,
            link = entity.link
        )
    }
}
