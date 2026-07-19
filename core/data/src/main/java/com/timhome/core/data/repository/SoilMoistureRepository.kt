package com.timhome.core.data.repository

import com.timhome.core.common.CallStatus
import com.timhome.core.database.entity.PotEntity
import com.timhome.core.database.entity.RoomClimateReadingEntity
import com.timhome.core.database.entity.RoomEntity
import com.timhome.core.database.entity.SoilMoistureReadingEntity
import com.timhome.core.database.entity.WateringEventEntity
import kotlinx.coroutines.flow.Flow

interface SoilMoistureRepository {
    fun getRooms(): Flow<List<RoomEntity>>

    fun getPots(): Flow<List<PotEntity>>

    fun getPotsForRoom(roomId: Int): Flow<List<PotEntity>>

    fun getLatestMoisture(potId: Int): Flow<SoilMoistureReadingEntity?>

    fun getLatestClimate(roomId: Int): Flow<RoomClimateReadingEntity?>

    fun getLatestWatering(potId: Int): Flow<WateringEventEntity?>

    suspend fun getRoom(roomId: Int): RoomEntity?

    suspend fun getPot(potId: Int): PotEntity?

    suspend fun saveRoom(room: RoomEntity)

    suspend fun deleteRoom(room: RoomEntity)

    suspend fun savePot(pot: PotEntity)

    suspend fun deletePot(pot: PotEntity)

    suspend fun waterPot(
        pot: PotEntity,
        room: RoomEntity,
    ): CallStatus<Unit>

    /**
     * Polls every configured room over REST, persists new moisture/climate readings and
     * returns the pots whose most recent watering turned out ineffective (moisture didn't
     * rise), so the caller can notify the user.
     */
    suspend fun pollAllRooms(): List<Pair<PotEntity, RoomEntity>>
}
