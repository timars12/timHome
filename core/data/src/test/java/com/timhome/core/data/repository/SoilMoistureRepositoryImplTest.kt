package com.timhome.core.data.repository

import com.timhome.core.database.AppDatabase
import com.timhome.core.database.dao.PotDao
import com.timhome.core.database.dao.RoomClimateReadingDao
import com.timhome.core.database.dao.RoomDao
import com.timhome.core.database.dao.SoilMoistureReadingDao
import com.timhome.core.database.dao.WateringEventDao
import com.timhome.core.database.entity.PotEntity
import com.timhome.core.database.entity.RoomEntity
import com.timhome.core.database.entity.SoilMoistureReadingEntity
import com.timhome.core.database.entity.WateringEventEntity
import com.timhome.core.network.api.SoilMoistureApi
import com.timhome.core.network.api.response.PotReading
import com.timhome.core.network.api.response.SoilMoistureStatusResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.net.SocketTimeoutException

class SoilMoistureRepositoryImplTest {
    private val database: AppDatabase = mockk()
    private val soilMoistureApi: SoilMoistureApi = mockk()

    private val roomDao: RoomDao = mockk(relaxed = true)
    private val potDao: PotDao = mockk(relaxed = true)
    private val readingDao: SoilMoistureReadingDao = mockk(relaxed = true)
    private val climateDao: RoomClimateReadingDao = mockk(relaxed = true)
    private val wateringDao: WateringEventDao = mockk(relaxed = true)

    private val room =
        RoomEntity(
            id = 1,
            name = "Кухня",
            ipAddress = "192.168.1.50",
            lastPollSuccessful = false,
            lastPollError = "попередня помилка",
        )
    private val pot = PotEntity(id = 10, roomId = 1, name = "Фікус", channel = 0)

    private lateinit var repository: SoilMoistureRepositoryImpl

    @Before
    fun setUp() {
        every { database.roomDao() } returns roomDao
        every { database.potDao() } returns potDao
        every { database.soilMoistureReadingDao() } returns readingDao
        every { database.roomClimateReadingDao() } returns climateDao
        every { database.wateringEventDao() } returns wateringDao

        coEvery { roomDao.getAll() } returns flowOf(listOf(room))
        coEvery { potDao.getForRoom(room.id) } returns flowOf(listOf(pot))
        coEvery { wateringDao.getUnresolved() } returns emptyList()

        repository = SoilMoistureRepositoryImpl(database, soilMoistureApi)
    }

    @Test
    fun `pollAllRooms saves readings and marks room connected on success`() =
        runTest {
            coEvery { soilMoistureApi.getData("http://${room.ipAddress}/data") } returns
                Response.success(
                    SoilMoistureStatusResponse(
                        bme = true,
                        temp = 24.0,
                        hum = 55.0,
                        pres = 1000.0,
                        pots = listOf(PotReading(id = 0, name = "Фікус", valid = true, pct = 40, threshold = 30, pump = false)),
                    ),
                )

            repository.pollAllRooms()

            coVerify { climateDao.insert(match { it.roomId == room.id && it.temperature == 24.0 }) }
            coVerify { readingDao.insert(match { it.potId == pot.id && it.moisturePercent == 40 }) }
            coVerify { roomDao.update(match { it.lastPollSuccessful && it.lastPollError == null }) }
        }

    @Test
    fun `pollAllRooms marks room disconnected on network failure`() =
        runTest {
            coEvery { soilMoistureApi.getData(any()) } throws SocketTimeoutException()

            repository.pollAllRooms()

            coVerify { roomDao.update(match { !it.lastPollSuccessful && it.lastPollError != null }) }
        }

    @Test
    fun `flags an unresolved watering event as ineffective when moisture barely changed`() =
        runTest {
            coEvery { soilMoistureApi.getData(any()) } returns
                Response.success(
                    SoilMoistureStatusResponse(
                        bme = true,
                        temp = 24.0,
                        hum = 55.0,
                        pres = 1000.0,
                        pots = listOf(PotReading(id = 0, name = "Фікус", valid = true, pct = 31, threshold = 30, pump = false)),
                    ),
                )
            val event = WateringEventEntity(id = 5, potId = pot.id, wateredAt = "2026-07-19T10:00", moistureBeforeWatering = 30)
            coEvery { wateringDao.getUnresolved() } returns listOf(event)
            coEvery { potDao.getById(pot.id) } returns pot
            coEvery { roomDao.getById(room.id) } returns room
            coEvery { readingDao.getLatestForPotOnce(pot.id) } returns
                SoilMoistureReadingEntity(id = 99, potId = pot.id, moisturePercent = 31, timestamp = "2026-07-19T10:31")

            val ineffective = repository.pollAllRooms()

            assertEquals(1, ineffective.size)
            assertEquals(pot.id, ineffective.first().first.id)
            coVerify { wateringDao.update(match { it.id == event.id && it.isEffective == false }) }
        }

    @Test
    fun `resolves a watering event as effective once moisture rises enough`() =
        runTest {
            coEvery { soilMoistureApi.getData(any()) } returns
                Response.success(
                    SoilMoistureStatusResponse(
                        bme = true,
                        temp = 24.0,
                        hum = 55.0,
                        pres = 1000.0,
                        pots = listOf(PotReading(id = 0, name = "Фікус", valid = true, pct = 45, threshold = 30, pump = false)),
                    ),
                )
            val event = WateringEventEntity(id = 5, potId = pot.id, wateredAt = "2026-07-19T10:00", moistureBeforeWatering = 30)
            coEvery { wateringDao.getUnresolved() } returns listOf(event)
            coEvery { potDao.getById(pot.id) } returns pot
            coEvery { roomDao.getById(room.id) } returns room
            coEvery { readingDao.getLatestForPotOnce(pot.id) } returns
                SoilMoistureReadingEntity(id = 99, potId = pot.id, moisturePercent = 45, timestamp = "2026-07-19T10:31")

            val ineffective = repository.pollAllRooms()

            assertEquals(0, ineffective.size)
            coVerify { wateringDao.update(match { it.id == event.id && it.isEffective == true }) }
        }
}
