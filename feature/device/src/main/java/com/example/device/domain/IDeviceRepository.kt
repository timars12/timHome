package com.example.device.domain

import com.example.device.domain.models.DeviceModel
import com.example.device.domain.models.DeviceWithModuleModel
import kotlinx.coroutines.flow.Flow

internal interface IDeviceRepository {
    suspend fun initAllDevices()

    fun getAllDevices(): Flow<List<DeviceModel>>

    suspend fun getSelectedDeviceById(deviceId: Int): DeviceModel

    suspend fun getDeviceWithModuleById(deviceId: Int): DeviceWithModuleModel
}
