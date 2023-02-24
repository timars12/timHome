package com.example.device.data.repository

import androidx.room.Transaction
import com.example.core.data.AppDatabase
import com.example.device.data.mappers.DeviceMapper
import com.example.device.data.mock.GenerateDate
import com.example.device.data.model.Device
import com.example.device.data.model.ModuleModel
import com.example.device.domain.IDeviceRepository
import com.example.device.domain.models.DeviceModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DeviceRepository @Inject constructor(
    private val database: AppDatabase,
    private val mapper: DeviceMapper,
    private val mock: GenerateDate
) : IDeviceRepository {

    @Suppress("SuspendFunWithFlowReturnType")
    override suspend fun getAllDevices(): Flow<ImmutableList<DeviceModel>> {
        saveDevicesToDB(mock.generateItems())
        return flow {
            database.deviceDao().getAllDevices().collect { entityList ->
                emit(entityList.map { mapper.convertEntityToModel(it) }.toImmutableList())
            }
        }
    }

    override suspend fun getSelectedDeviceById(deviceId: Int): DeviceModel {
        return database.deviceDao().getSelectedDeviceById(deviceId).run {
            mapper.convertEntityToModel(this)
        }
    }

    @Transaction
    private suspend fun saveDevicesToDB(list: List<Device>) {
        list.map { device ->
            mapper.convertToEntityModel(device).also { entity ->
                database.deviceDao().saveDeviceToDB(entity).also { deviceId ->
                    device.modules.map { module ->
                        database.deviceDao().saveModuleToDB(
                            mapper.convertModuleToModuleEntity(
                                deviceId.toInt(),
                                module
                            )
                        )
                    }
                }
            }
        }
    }

    override suspend fun getDeviceWithModuleById(deviceId: Int): DeviceWithModuleModel {
        return database.deviceDao().getDeviceWithModuleById(deviceId).let { modelEntity ->
            val device = mapper.convertEntityToModel(modelEntity.device)
            val module = modelEntity.module.map {
                mapper.convertModuleEntityToModel(it)
            }
            DeviceWithModuleModel(device, module)
        }
    }
}

data class DeviceWithModuleModel(val device: DeviceModel, val modules: List<ModuleModel>)
