package com.timhome.device.ui.listDevice

import androidx.lifecycle.SavedStateHandle
import com.google.firebase.analytics.FirebaseAnalytics
import com.timhome.core.common.NavigationDispatcher
import com.timhome.device.data.repository.DeviceRepository
import com.timhome.device.domain.models.DeviceModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DeviceListViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository: DeviceRepository = mockk(relaxed = true)
    private val navigationDispatcher = NavigationDispatcher()
    private val firebaseAnalytics: FirebaseAnalytics = mockk(relaxed = true)

    private val device =
        DeviceModel(id = 1, image = 0, title = "Device", totalPrice = "$10", dateCreated = "today")

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        DeviceListViewModel(SavedStateHandle(), navigationDispatcher, repository, dispatcher, firebaseAnalytics)

    @Test
    fun `exposes loaded devices in ui state`() =
        runTest {
            every { repository.getAllDevices() } returns flowOf(persistentListOf(device))

            val viewModel = createViewModel()
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(persistentListOf(device), viewModel.uiState.value.devices)
            coVerify(exactly = 0) { repository.initAllDevices() }
        }

    @Test
    fun `seeds devices when repository is empty`() =
        runTest {
            every { repository.getAllDevices() } returns flowOf(persistentListOf())

            val viewModel = createViewModel()
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(persistentListOf<DeviceModel>(), viewModel.uiState.value.devices)
            coVerify { repository.initAllDevices() }
        }

    @Test
    fun `navigateToDetailScreen emits a navigation command`() =
        runTest {
            every { repository.getAllDevices() } returns flowOf(persistentListOf(device))

            val viewModel = createViewModel()

            viewModel.navigateToDetailScreen(device)

            assertNotNull(navigationDispatcher.navigationEmitter.tryReceive().getOrNull())
        }
}
