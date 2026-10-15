package com.timhome.device.ui.listDevice

import androidx.lifecycle.SavedStateHandle
import com.google.firebase.analytics.FirebaseAnalytics
import com.timhome.core.common.navigation.ChannelNavigator
import com.timhome.core.common.navigation.DeviceDetail
import com.timhome.core.common.navigation.Devices
import com.timhome.core.common.navigation.IssuedCommand
import com.timhome.core.common.navigation.NavigationCommand.GoTo
import com.timhome.device.domain.IDeviceRepository
import com.timhome.device.domain.models.DeviceModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DeviceListViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository: IDeviceRepository = mockk(relaxed = true)
    private val channelNavigator = ChannelNavigator()
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

    /** Creates the ViewModel and subscribes to its state, as the screen does. */
    private fun TestScope.createViewModel(): DeviceListViewModel {
        val viewModel =
            DeviceListViewModel(SavedStateHandle(), channelNavigator.issuedFrom(Devices), repository, dispatcher, firebaseAnalytics)
        backgroundScope.launch { viewModel.uiState.collect {} }
        return viewModel
    }

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

            assertEquals(
                IssuedCommand(GoTo(DeviceDetail(device.id)), origin = Devices),
                channelNavigator.commands.first(),
            )
        }
}
