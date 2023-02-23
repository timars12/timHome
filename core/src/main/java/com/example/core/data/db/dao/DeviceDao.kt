package com.example.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.core.data.db.entity.DeviceEntity
import com.example.core.data.db.entity.DeviceWithModule
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
    suspend fun getDeviceWithModuleById(deviceId: Int): DeviceWithModule
}
