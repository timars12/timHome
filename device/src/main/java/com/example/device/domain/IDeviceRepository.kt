package com.example.device.domain

import com.example.device.data.repository.DeviceWithModuleModel
import com.example.device.domain.models.DeviceModel
import kotlinx.coroutines.flow.Flow

interface IDeviceRepository {
    @Suppress("SuspendFunWithFlowReturnType")
    suspend fun getAllDevices(): Flow<List<DeviceModel>>

    suspend fun getSelectedDeviceById(deviceId: Int): DeviceModel

    suspend fun getDeviceWithModuleById(deviceId: Int): DeviceWithModuleModel
}
