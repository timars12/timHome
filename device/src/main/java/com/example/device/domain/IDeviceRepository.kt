package com.example.device.domain

import com.example.device.domain.models.DeviceModel
import kotlinx.coroutines.flow.Flow

interface IDeviceRepository {
    suspend fun getAllDevices(): Flow<List<DeviceModel>>
}
