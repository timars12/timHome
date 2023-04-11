package com.example.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.data.db.entity.CarbonDioxideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CarbonDioxideDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCO2LevelToDB(device: CarbonDioxideEntity)

    @Query("select * from carbon_dioxide ORDER BY date DESC limit 10")
    fun getAllCO2Levels(): Flow<List<CarbonDioxideEntity>>
}
