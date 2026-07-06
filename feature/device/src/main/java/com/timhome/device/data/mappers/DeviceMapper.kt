package com.timhome.device.data.mappers

import com.timhome.core.data.db.entity.DeviceEntity
import com.timhome.core.data.db.entity.ModuleEntity
import com.timhome.core.di.scope.FeatureScope
import com.timhome.device.R
import com.timhome.device.data.model.Device
import com.timhome.device.data.model.ModuleModel
import com.timhome.device.domain.models.DeviceModel
import javax.inject.Inject

@FeatureScope
internal class DeviceMapper
    @Inject
    constructor() {
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
                dateCreated = device.dateCreated,
            )
        }

        fun convertModuleToModuleEntity(
            deviceId: Int,
            model: ModuleModel,
        ): ModuleEntity {
            return ModuleEntity(
                id = model.id,
                deviceId = deviceId,
                image = model.image,
                title = model.title,
                price = model.price,
                link = model.link,
                isSelectToBuy = model.isSelectToBuy,
            )
        }

        fun convertEntityToModel(entity: DeviceEntity): DeviceModel {
            return DeviceModel(
                id = entity.id,
                image = getImage(entity.image),
                title = entity.title,
                description = entity.description,
                totalPrice = entity.totalPrice,
                rating = entity.rating,
                isFavorite = entity.isFavorite,
                dateCreated = entity.dateCreated,
            )
        }

        fun convertModuleEntityToModel(entity: ModuleEntity): ModuleModel {
            return ModuleModel(
                id = entity.id,
                image = entity.image,
                title = entity.title,
                price = entity.price,
                link = entity.link,
                isSelectToBuy = entity.isSelectToBuy,
            )
        }

        // TODO better to use enum
        private fun getImage(type: String?): Int {
            if (type.isNullOrEmpty()) return 0
            return when (type) {
                "proj_1" -> R.mipmap.ic_arduino_proj_1
                "proj_2" -> R.mipmap.ic_arduino_proj_2
                else -> R.mipmap.ic_arduino_proj_3
            }
        }
    }
