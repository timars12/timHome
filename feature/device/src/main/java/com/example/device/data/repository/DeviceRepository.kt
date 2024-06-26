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
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DeviceRepository
    @Inject
    constructor(
        private val database: AppDatabase,
        private val mapper: DeviceMapper,
        private val mock: GenerateDate,
    ) : IDeviceRepository {
        override suspend fun initAllDevices() {
            mock.generateItems().collect { responseList -> saveDevicesToDB(responseList) }
        }

        override fun getAllDevices(): Flow<ImmutableList<DeviceModel>> {
            return database.deviceDao().getAllDevices().map { list ->
                list.map {
                    mapper.convertEntityToModel(it)
                }.toImmutableList()
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
                                    module,
                                ),
                            )
                        }
                    }
                }
            }
        }

        override suspend fun getDeviceById(deviceId: Int): DeviceModel {
            return database.deviceDao().getDeviceById(deviceId).let {
                mapper.convertEntityToModel(it)
            }
        }

        override suspend fun selectModuleToBuy(module: ModuleModel) {
            database.deviceDao().updateModuleInDB(!module.isSelectToBuy, module.id)
        }

        override fun getModuleToBuyByDeviceId(deviceId: Int): Flow<List<ModuleModel>> {
            return database.deviceDao().getModuleByDeviceId(deviceId).map { list ->
                list.map {
                    mapper.convertModuleEntityToModel(it)
                }
            }
        }

        override fun getSelectedModuleToBuyByDeviceId(deviceId: Int): Flow<List<ModuleModel>> {
            return database.deviceDao().getSelectedModuleToBuyByDeviceId(deviceId).map { list ->
                list.map {
                    mapper.convertModuleEntityToModel(it)
                }
            }
        }

        override fun getSelectedModuleToBuy(): Flow<List<ModuleModel>> {
            return database.deviceDao().getSelectedModuleToBuy().map { list ->
                list.map {
                    mapper.convertModuleEntityToModel(it)
                }
            }
        }
    }
