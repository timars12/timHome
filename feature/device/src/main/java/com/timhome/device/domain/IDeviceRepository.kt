package com.timhome.device.domain

import com.timhome.device.data.model.ModuleModel
import com.timhome.device.domain.models.DeviceModel
import kotlinx.coroutines.flow.Flow

internal interface IDeviceRepository {
    suspend fun initAllDevices()

    fun getAllDevices(): Flow<List<DeviceModel>>

    suspend fun getSelectedDeviceById(deviceId: Int): DeviceModel

    suspend fun getDeviceById(deviceId: Int): DeviceModel

    fun getModuleToBuyByDeviceId(deviceId: Int): Flow<List<ModuleModel>>

    fun getSelectedModuleToBuy(): Flow<List<ModuleModel>>

    fun getSelectedModuleToBuyByDeviceId(deviceId: Int): Flow<List<ModuleModel>>

    suspend fun selectModuleToBuy(module: ModuleModel)
}
