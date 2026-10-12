package com.timhome.device.ui.device

import androidx.lifecycle.SavedStateHandle
import com.timhome.core.common.navigation.BuyModule
import com.timhome.core.common.navigation.ChannelNavigator
import com.timhome.core.common.navigation.DeviceDetail
import com.timhome.core.common.navigation.IssuedCommand
import com.timhome.core.common.navigation.NavigationCommand.Back
import com.timhome.core.common.navigation.NavigationCommand.GoTo
import com.timhome.device.data.model.ModuleModel
import com.timhome.device.domain.IDeviceRepository
import com.timhome.device.domain.models.DeviceModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeviceDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository: IDeviceRepository = mockk(relaxed = true)
    private val channelNavigator = ChannelNavigator()

    private val device =
        DeviceModel(id = 1, image = 0, title = "Device", totalPrice = "$10", dateCreated = "today")
    private val module =
        ModuleModel(id = 5, title = "Module", price = "$3", link = "link", isSelectToBuy = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { repository.getDeviceById(any()) } returns device
        coEvery { repository.getModuleToBuyByDeviceId(any()) } returns flowOf(listOf(module))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(deviceId: Int = 1): DeviceDetailViewModel {
        val route = DeviceDetail(deviceId)
        return DeviceDetailViewModel(SavedStateHandle(), route, channelNavigator.issuedFrom(route), repository, dispatcher)
    }

    @Test
    fun `initial state is empty before loading`() =
        runTest {
            val viewModel = createViewModel()

            assertEquals(DeviceDetailUiState(), viewModel.uiState.value)
        }

    @Test
    fun `loads device and modules for the routed deviceId`() =
        runTest {
            val viewModel = createViewModel(deviceId = 1)
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(device, viewModel.uiState.value.device)
            assertEquals(listOf(module), viewModel.uiState.value.modules)
            coVerify { repository.getDeviceById(1) }
        }

    @Test
    fun `selectModuleToBuy delegates to repository`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.selectModuleToBuy(module)
            dispatcher.scheduler.advanceUntilIdle()

            coVerify { repository.selectModuleToBuy(module) }
        }

    @Test
    fun `buyModules emits a navigation command`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.buyModules()

            assertEquals(
                IssuedCommand(GoTo(BuyModule(1)), origin = DeviceDetail(1)),
                channelNavigator.commands.first(),
            )
        }

    @Test
    fun `goBack emits a navigation command`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.goBack()

            assertEquals(IssuedCommand(Back, origin = DeviceDetail(1)), channelNavigator.commands.first())
        }
}
