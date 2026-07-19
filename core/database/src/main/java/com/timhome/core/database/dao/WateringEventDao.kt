package com.timhome.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timhome.core.database.entity.WateringEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WateringEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: WateringEventEntity)

    @Query("select * from watering_event where potId = :potId ORDER BY id DESC LIMIT 1")
    fun getLatestForPot(potId: Int): Flow<WateringEventEntity?>
}
