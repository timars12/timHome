package com.example.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.data.db.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDeviceToDB(device: DeviceEntity)

    @Query("select * from devices")
    fun getAllDevices(): Flow<List<DeviceEntity>>
}