package com.example.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.core.data.db.entity.DeviceEntity
import com.example.core.data.db.entity.ModuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDeviceToDB(device: DeviceEntity): Long

    @Query("select * from devices")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Query("select * from devices where id = :deviceId LIMIT 1")
    suspend fun getSelectedDeviceById(deviceId: Int): DeviceEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveModuleToDB(module: ModuleEntity)

    @Transaction
    @Query("select * from devices where devices.id = :deviceId")
    suspend fun getDeviceById(deviceId: Int): DeviceEntity

    @Query("UPDATE modules SET isSelectToBuy = :isSelectToBuy WHERE id =:moduleId")
    suspend fun updateModuleInDB(
        isSelectToBuy: Boolean,
        moduleId: Int,
    )

    @Query("select * from modules where modules.device_id =:deviceId")
    fun getModuleByDeviceId(deviceId: Int): Flow<List<ModuleEntity>>

    @Query("select * from modules where modules.device_id =:deviceId AND modules.isSelectToBuy = 1")
    fun getSelectedModuleToBuyByDeviceId(deviceId: Int): Flow<List<ModuleEntity>>

    @Query("select * from modules where modules.isSelectToBuy = 1")
    fun getSelectedModuleToBuy(): Flow<List<ModuleEntity>>
}
