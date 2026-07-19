package com.timhome.soilmoisture.ui.list

import androidx.lifecycle.SavedStateHandle
import com.timhome.core.common.CallStatus
import com.timhome.core.common.NavigationDispatcher
import com.timhome.core.data.repository.SoilMoistureRepository
import com.timhome.core.database.entity.PotEntity
import com.timhome.core.database.entity.RoomClimateReadingEntity
import com.timhome.core.database.entity.RoomEntity
import com.timhome.core.database.entity.SoilMoistureReadingEntity
import com.timhome.core.database.entity.WateringEventEntity
import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SoilMoistureListViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository: SoilMoistureRepository = mockk(relaxed = true)
    private val navigationDispatcher: NavigationDispatcher = mockk(relaxed = true)
    private val firebaseAnalytics: FirebaseAnalytics = mockk(relaxed = true)

    private val room = RoomEntity(id = 1, name = "Кухня", ipAddress = "192.168.1.50")
    private val pot = PotEntity(id = 10, roomId = 1, name = "Фікус", channel = 0)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        SoilMoistureListViewModel(repository, navigationDispatcher, dispatcher, SavedStateHandle(), firebaseAnalytics)

    @Test
    fun `groups pots under their room with latest readings`() =
        runTest {
            every { repository.getRooms() } returns flowOf(listOf(room))
            every { repository.getPots() } returns flowOf(listOf(pot))
            every { repository.getLatestClimate(room.id) } returns
                flowOf(RoomClimateReadingEntity(roomId = room.id, temperature = 23.5, humidity = 48.0, timestamp = "t"))
            every { repository.getLatestMoisture(pot.id) } returns
                flowOf(SoilMoistureReadingEntity(potId = pot.id, moisturePercent = 42, timestamp = "t"))
            every { repository.getLatestWatering(pot.id) } returns
                flowOf(WateringEventEntity(potId = pot.id, wateredAt = "t0", moistureBeforeWatering = 30, isEffective = true))

            val viewModel = createViewModel()
            dispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(1, state.rooms.size)
            val roomState = state.rooms.first()
            assertEquals("Кухня", roomState.name)
            assertEquals(23.5, roomState.temperature)
            assertEquals(true, roomState.isConnected)
            assertEquals(1, roomState.pots.size)
            assertEquals(42, roomState.pots.first().moisturePercent)
            assertEquals(true, roomState.pots.first().lastWateringEffective)
            assertEquals(false, state.isLoading)
        }

    @Test
    fun `shows no rooms when nothing is configured`() =
        runTest {
            every { repository.getRooms() } returns flowOf(emptyList())
            every { repository.getPots() } returns flowOf(emptyList())

            val viewModel = createViewModel()
            dispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(0, state.rooms.size)
            assertEquals(false, state.isLoading)
        }

    @Test
    fun `waterPot surfaces a snackbar message on error`() =
        runTest {
            every { repository.getRooms() } returns flowOf(listOf(room))
            every { repository.getPots() } returns flowOf(listOf(pot))
            every { repository.getLatestClimate(room.id) } returns flowOf(null)
            every { repository.getLatestMoisture(pot.id) } returns flowOf(null)
            every { repository.getLatestWatering(pot.id) } returns flowOf(null)
            coEvery { repository.getPot(pot.id) } returns pot
            coEvery { repository.getRoom(room.id) } returns room
            coEvery { repository.waterPot(pot, room) } returns CallStatus.Error("Немає зв'язку з ESP32")

            val viewModel = createViewModel()
            dispatcher.scheduler.advanceUntilIdle()

            viewModel.waterPot(pot.id)
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals("Немає зв'язку з ESP32", viewModel.uiState.value.snackbarMessage)

            viewModel.consumeSnackbarMessage()
            assertNull(viewModel.uiState.value.snackbarMessage)
        }
}
