package com.timhome.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timhome.core.database.entity.PotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pot: PotEntity): Long

    @Update
    suspend fun update(pot: PotEntity)

    @Delete
    suspend fun delete(pot: PotEntity)

    @Query("select * from pot ORDER BY name")
    fun getAll(): Flow<List<PotEntity>>

    @Query("select * from pot where roomId = :roomId ORDER BY name")
    fun getForRoom(roomId: Int): Flow<List<PotEntity>>

    @Query("select * from pot where id = :potId")
    suspend fun getById(potId: Int): PotEntity?
}
