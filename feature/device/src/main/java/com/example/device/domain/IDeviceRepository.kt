package com.example.device.domain

import com.example.device.data.model.ModuleModel
import com.example.device.domain.models.DeviceModel
import kotlinx.coroutines.flow.Flow

internal interface IDeviceRepository {
    suspend fun initAllDevices()

    fun getAllDevices(): Flow<List<DeviceModel>>

    suspend fun getSelectedDeviceById(deviceId: Int): DeviceModel

    suspend fun getDeviceById(deviceId: Int): DeviceModel

    fun getModuleToBuyByDeviceId(deviceId: Int): Flow<List<ModuleModel>>

    suspend fun selectModuleToBuy(module: ModuleModel)
}
