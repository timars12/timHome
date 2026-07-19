package com.timhome.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timhome.core.database.entity.SoilMoistureReadingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SoilMoistureReadingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: SoilMoistureReadingEntity)

    @Query("select * from soil_moisture_reading where potId = :potId ORDER BY id DESC LIMIT 1")
    fun getLatestForPot(potId: Int): Flow<SoilMoistureReadingEntity?>

    @Query("select * from soil_moisture_reading where potId = :potId ORDER BY id DESC LIMIT 1")
    suspend fun getLatestForPotOnce(potId: Int): SoilMoistureReadingEntity?

    @Query("select * from soil_moisture_reading where potId = :potId ORDER BY id DESC limit 20")
    fun getHistoryForPot(potId: Int): Flow<List<SoilMoistureReadingEntity>>
}
