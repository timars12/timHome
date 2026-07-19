package com.timhome.core.data.repository

import com.timhome.core.common.CallStatus
import com.timhome.core.common.Constant.CODE_200
import com.timhome.core.database.AppDatabase
import com.timhome.core.database.entity.PotEntity
import com.timhome.core.database.entity.RoomClimateReadingEntity
import com.timhome.core.database.entity.RoomEntity
import com.timhome.core.database.entity.SoilMoistureReadingEntity
import com.timhome.core.database.entity.WateringEventEntity
import com.timhome.core.network.api.SoilMoistureApi
import com.timhome.core.network.api.response.SoilMoistureStatusResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDateTime
import javax.inject.Inject

class SoilMoistureRepositoryImpl
    @Inject
    constructor(
        private val database: AppDatabase,
        private val soilMoistureApi: SoilMoistureApi,
    ) : SoilMoistureRepository {
        override fun getRooms(): Flow<List<RoomEntity>> = database.roomDao().getAll()

        override fun getPots(): Flow<List<PotEntity>> = database.potDao().getAll()

        override fun getPotsForRoom(roomId: Int): Flow<List<PotEntity>> = database.potDao().getForRoom(roomId)

        override fun getLatestMoisture(potId: Int): Flow<SoilMoistureReadingEntity?> =
            database.soilMoistureReadingDao().getLatestForPot(potId)

        override fun getLatestClimate(roomId: Int): Flow<RoomClimateReadingEntity?> =
            database.roomClimateReadingDao().getLatestForRoom(roomId)

        override fun getLatestWatering(potId: Int): Flow<WateringEventEntity?> =
            database.wateringEventDao().getLatestForPot(potId)

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
            return try {
                val response = soilMoistureApi.waterPot("http://${room.ipAddress}/pump?id=${pot.channel}")
                if (response.isSuccessful) {
                    database.wateringEventDao().insert(
                        WateringEventEntity(potId = pot.id, wateredAt = LocalDateTime.now().toString()),
                    )
                    CallStatus.Success(Unit)
                } else {
                    CallStatus.Error(error = "HTTP ${response.code()}")
                }
            } catch (exception: Exception) {
                CallStatus.Error(error = exception.toConnectionErrorMessage())
            }
        }

        override suspend fun pollAllRooms(): List<Pair<PotEntity, RoomEntity>> {
            val rooms = database.roomDao().getAll().first()
            val toNotify = mutableListOf<Pair<PotEntity, RoomEntity>>()
            rooms.forEach { room -> toNotify += pollRoom(room) }
            return toNotify
        }

        private suspend fun pollRoom(room: RoomEntity): List<Pair<PotEntity, RoomEntity>> {
            if (room.ipAddress.isBlank()) {
                markRoomStatus(room, isSuccessful = false, error = "IP-адреса не налаштована")
                return emptyList()
            }
            return try {
                val response = soilMoistureApi.getData("http://${room.ipAddress}/data")
                val body = response.body()
                if (response.code() == CODE_200 && body != null) {
                    markRoomStatus(room, isSuccessful = true, error = null)
                    saveReadingsAndDetectAlarms(room, body)
                } else {
                    markRoomStatus(room, isSuccessful = false, error = "HTTP ${response.code()}")
                    emptyList()
                }
            } catch (exception: Exception) {
                markRoomStatus(room, isSuccessful = false, error = exception.toConnectionErrorMessage())
                emptyList()
            }
        }

        /**
         * Persists the latest readings and mirrors the ESP32's per-pot alarm flag onto
         * [PotEntity.alarmActive]. Returns the pots whose alarm just flipped false -> true,
         * so the caller notifies exactly once per failure instead of on every poll.
         */
        private suspend fun saveReadingsAndDetectAlarms(
            room: RoomEntity,
            body: SoilMoistureStatusResponse,
        ): List<Pair<PotEntity, RoomEntity>> {
            database.roomClimateReadingDao().insert(
                RoomClimateReadingEntity(roomId = room.id, temperature = body.temp, humidity = body.hum),
            )
            val roomPots = database.potDao().getForRoom(room.id).first()
            val toNotify = mutableListOf<Pair<PotEntity, RoomEntity>>()
            body.pots.forEach { reading ->
                val pot = roomPots.firstOrNull { it.channel == reading.id } ?: return@forEach
                database.soilMoistureReadingDao().insert(
                    SoilMoistureReadingEntity(potId = pot.id, moisturePercent = reading.pct),
                )
                if (reading.waterAlarm != pot.alarmActive) {
                    database.potDao().update(pot.copy(alarmActive = reading.waterAlarm))
                    if (reading.waterAlarm) toNotify.add(pot to room)
                }
            }
            return toNotify
        }

        private suspend fun markRoomStatus(
            room: RoomEntity,
            isSuccessful: Boolean,
            error: String?,
        ) {
            if (room.lastPollSuccessful == isSuccessful && room.lastPollError == error) return
            database.roomDao().update(room.copy(lastPollSuccessful = isSuccessful, lastPollError = error))
        }

        private fun Exception.toConnectionErrorMessage(): String =
            when (this) {
                is UnknownHostException -> "Пристрій не знайдено в мережі"
                is SocketTimeoutException -> "Тайм-аут з'єднання з ESP32"
                is IOException -> "Немає зв'язку з ESP32"
                else -> message ?: "Невідома помилка з'єднання"
            }
    }
