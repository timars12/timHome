package com.timhome.core.data.repository

import com.timhome.core.database.AppDatabase
import com.timhome.core.database.dao.PotDao
import com.timhome.core.database.dao.RoomClimateReadingDao
import com.timhome.core.database.dao.RoomDao
import com.timhome.core.database.dao.SoilMoistureReadingDao
import com.timhome.core.database.dao.WateringEventDao
import com.timhome.core.database.entity.PotEntity
import com.timhome.core.database.entity.RoomEntity
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
    private val pot = PotEntity(id = 10, roomId = 1, name = "Фікус", channel = 0, alarmActive = false)

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

        repository = SoilMoistureRepositoryImpl(database, soilMoistureApi)
    }

    private fun statusWith(
        pct: Int,
        waterAlarm: Boolean,
    ) = SoilMoistureStatusResponse(
        bme = true,
        temp = 24.0,
        hum = 55.0,
        pres = 1000.0,
        pots = listOf(PotReading(id = 0, name = "Фікус", valid = true, pct = pct, threshold = 30, pump = false, waterAlarm = waterAlarm)),
    )

    @Test
    fun `pollAllRooms saves readings and marks room connected on success`() =
        runTest {
            coEvery { soilMoistureApi.getData("http://${room.ipAddress}/data") } returns
                Response.success(statusWith(pct = 40, waterAlarm = false))

            val notify = repository.pollAllRooms()

            assertEquals(0, notify.size)
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
    fun `raises a notification and latches the flag when the alarm turns on`() =
        runTest {
            // pot starts with alarmActive = false (from setUp)
            coEvery { soilMoistureApi.getData(any()) } returns Response.success(statusWith(pct = 31, waterAlarm = true))

            val notify = repository.pollAllRooms()

            assertEquals(1, notify.size)
            assertEquals(pot.id, notify.first().first.id)
            coVerify { potDao.update(match { it.id == pot.id && it.alarmActive }) }
        }

    @Test
    fun `does not re-notify while the alarm stays on`() =
        runTest {
            val alarmingPot = pot.copy(alarmActive = true)
            coEvery { potDao.getForRoom(room.id) } returns flowOf(listOf(alarmingPot))
            coEvery { soilMoistureApi.getData(any()) } returns Response.success(statusWith(pct = 31, waterAlarm = true))

            val notify = repository.pollAllRooms()

            assertEquals(0, notify.size)
            coVerify(exactly = 0) { potDao.update(any()) }
        }

    @Test
    fun `clears the flag without notifying when the alarm turns off`() =
        runTest {
            val alarmingPot = pot.copy(alarmActive = true)
            coEvery { potDao.getForRoom(room.id) } returns flowOf(listOf(alarmingPot))
            coEvery { soilMoistureApi.getData(any()) } returns Response.success(statusWith(pct = 55, waterAlarm = false))

            val notify = repository.pollAllRooms()

            assertEquals(0, notify.size)
            coVerify { potDao.update(match { it.id == pot.id && !it.alarmActive }) }
        }
}
