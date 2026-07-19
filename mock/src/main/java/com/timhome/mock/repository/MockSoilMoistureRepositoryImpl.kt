package com.timhome.mock.repository

import com.timhome.core.common.CallStatus
import com.timhome.core.common.Constant.SOIL_MOISTURE_MIN_EFFECTIVE_INCREASE_PERCENT
import com.timhome.core.data.repository.SoilMoistureRepository
import com.timhome.core.database.AppDatabase
import com.timhome.core.database.entity.PotEntity
import com.timhome.core.database.entity.RoomClimateReadingEntity
import com.timhome.core.database.entity.RoomEntity
import com.timhome.core.database.entity.SoilMoistureReadingEntity
import com.timhome.core.database.entity.WateringEventEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject

private const val MOCK_DEFAULT_MOISTURE = 50
private const val MOCK_MIN_MOISTURE = 0
private const val MOCK_MAX_MOISTURE = 100
private const val MOCK_MOISTURE_DRIFT_MIN = -5
private const val MOCK_MOISTURE_DRIFT_MAX = 2
private const val MOCK_TEMPERATURE_MIN = 20.0
private const val MOCK_TEMPERATURE_MAX = 27.0
private const val MOCK_HUMIDITY_MIN = 40.0
private const val MOCK_HUMIDITY_MAX = 65.0

/**
 * Simulates the ESP32 REST layer so the soil-moisture screen has data to show when the
 * app is running in mock mode. Room/pot management is still backed by the real database,
 * mirroring [com.timhome.core.data.repository.SoilMoistureRepositoryImpl].
 */
open class MockSoilMoistureRepositoryImpl
    @Inject
    constructor(
        private val database: AppDatabase,
    ) : SoilMoistureRepository {
        override fun getRooms(): Flow<List<RoomEntity>> = database.roomDao().getAll()

        override fun getPots(): Flow<List<PotEntity>> = database.potDao().getAll()

        override fun getPotsForRoom(roomId: Int): Flow<List<PotEntity>> = database.potDao().getForRoom(roomId)

        override fun getLatestMoisture(potId: Int) = database.soilMoistureReadingDao().getLatestForPot(potId)

        override fun getLatestClimate(roomId: Int) = database.roomClimateReadingDao().getLatestForRoom(roomId)

        override fun getLatestWatering(potId: Int) = database.wateringEventDao().getLatestForPot(potId)

        override suspend fun getRoom(roomId: Int): RoomEntity? = database.roomDao().getById(roomId)

        override suspend fun getPot(potId: Int): PotEntity? = database.potDao().getById(potId)

        override suspend fun saveRoom(room: RoomEntity) {
            if (room.id == 0) database.roomDao().insert(room) else database.roomDao().update(room)
        }

        override suspend fun deleteRoom(room: RoomEntity) = database.roomDao().delete(room)

        override suspend fun savePot(pot: PotEntity) {
            if (pot.id == 0) database.potDao().insert(pot) else database.potDao().update(pot)
        }

        override suspend fun deletePot(pot: PotEntity) = database.potDao().delete(pot)

        override suspend fun waterPot(
            pot: PotEntity,
            room: RoomEntity,
        ): CallStatus<Unit> {
            val lastMoisture = database.soilMoistureReadingDao().getLatestForPotOnce(pot.id)?.moisturePercent ?: MOCK_DEFAULT_MOISTURE
            database.wateringEventDao().insert(
                WateringEventEntity(
                    potId = pot.id,
                    wateredAt = LocalDateTime.now().toString(),
                    moistureBeforeWatering = lastMoisture,
                ),
            )
            return CallStatus.Success(Unit)
        }

        override suspend fun pollAllRooms(): List<Pair<PotEntity, RoomEntity>> {
            val now = LocalDateTime.now().toString()
            val rooms = database.roomDao().getAll().first()
            rooms.forEach { room ->
                if (!room.lastPollSuccessful || room.lastPollError != null) {
                    database.roomDao().update(room.copy(lastPollSuccessful = true, lastPollError = null))
                }
                database.roomClimateReadingDao().insert(
                    RoomClimateReadingEntity(
                        roomId = room.id,
                        temperature = randomDouble(MOCK_TEMPERATURE_MIN, MOCK_TEMPERATURE_MAX),
                        humidity = randomDouble(MOCK_HUMIDITY_MIN, MOCK_HUMIDITY_MAX),
                        timestamp = now,
                    ),
                )
                database.potDao().getForRoom(room.id).first().forEach { pot ->
                    val previous = database.soilMoistureReadingDao().getLatestForPotOnce(pot.id)?.moisturePercent ?: MOCK_DEFAULT_MOISTURE
                    val drifted =
                        (previous + randomInt(MOCK_MOISTURE_DRIFT_MIN, MOCK_MOISTURE_DRIFT_MAX))
                            .coerceIn(MOCK_MIN_MOISTURE, MOCK_MAX_MOISTURE)
                    database.soilMoistureReadingDao().insert(
                        SoilMoistureReadingEntity(potId = pot.id, moisturePercent = drifted, timestamp = now),
                    )
                }
            }
            return resolveWateringEffectiveness()
        }

        private suspend fun resolveWateringEffectiveness(): List<Pair<PotEntity, RoomEntity>> {
            val unresolved = database.wateringEventDao().getUnresolved()
            val ineffective = mutableListOf<Pair<PotEntity, RoomEntity>>()
            unresolved.forEach { event ->
                val pot = database.potDao().getById(event.potId) ?: return@forEach
                val latestReading = database.soilMoistureReadingDao().getLatestForPotOnce(pot.id) ?: return@forEach
                if (latestReading.timestamp <= event.wateredAt) return@forEach

                val increase = latestReading.moisturePercent - event.moistureBeforeWatering
                val isEffective = increase >= SOIL_MOISTURE_MIN_EFFECTIVE_INCREASE_PERCENT
                database.wateringEventDao().update(event.copy(isEffective = isEffective))

                if (!isEffective) {
                    val room = database.roomDao().getById(pot.roomId)
                    if (room != null) ineffective.add(pot to room)
                }
            }
            return ineffective
        }

        private fun randomDouble(
            from: Double,
            until: Double,
        ): Double = from + Math.random() * (until - from)

        private fun randomInt(
            from: Int,
            until: Int,
        ): Int = from + (Math.random() * (until - from + 1)).toInt()
    }
