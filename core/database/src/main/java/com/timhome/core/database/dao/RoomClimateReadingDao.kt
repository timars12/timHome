package com.timhome.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timhome.core.database.entity.RoomClimateReadingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomClimateReadingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: RoomClimateReadingEntity)

    @Query("select * from room_climate_reading where roomId = :roomId ORDER BY id DESC LIMIT 1")
    fun getLatestForRoom(roomId: Int): Flow<RoomClimateReadingEntity?>
}
