package com.timhome.device.ui.buyModule

import androidx.lifecycle.SavedStateHandle
import com.timhome.core.common.navigation.BuyModule
import com.timhome.core.common.navigation.ChannelNavigator
import com.timhome.core.common.navigation.IssuedCommand
import com.timhome.core.common.navigation.NavigationCommand.Back
import com.timhome.device.data.model.ModuleModel
import com.timhome.device.domain.IDeviceRepository
import com.timhome.device.domain.models.DeviceModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.math.BigDecimal

@RunWith(RobolectricTestRunner::class)
class BuyModuleViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository: IDeviceRepository = mockk(relaxed = true)
    private val channelNavigator = ChannelNavigator()

    private val device =
        DeviceModel(id = 1, image = 0, title = "Device", totalPrice = "$10", dateCreated = "today")
    private val moduleA =
        ModuleModel(id = 1, title = "A", price = "$3", link = "l", isSelectToBuy = true)
    private val moduleB =
        ModuleModel(id = 2, title = "B", price = "$4", link = "l", isSelectToBuy = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { repository.getDeviceById(any()) } returns device
        every { repository.getSelectedModuleToBuyByDeviceId(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(deviceId: Int = 1): BuyModuleViewModel {
        val route = BuyModule(deviceId)
        return BuyModuleViewModel(SavedStateHandle(), route, channelNavigator.issuedFrom(route), repository)
    }

    @Test
    fun `sums prices and exposes modules for the routed device`() =
        runTest {
            every { repository.getSelectedModuleToBuyByDeviceId(7) } returns flowOf(listOf(moduleA, moduleB))

            val viewModel = createViewModel(deviceId = 7)
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(BigDecimal(7), viewModel.uiState.value.totalPrice)
            assertEquals(listOf(moduleA, moduleB), viewModel.uiState.value.modules)
        }

    @Test
    fun `a device that exists is not reported missing`() =
        runTest {
            val viewModel = createViewModel(deviceId = 1)
            dispatcher.scheduler.advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isDeviceMissing)
            coVerify(exactly = 1) { repository.getDeviceById(1) }
        }

    @Test
    fun `a device that does not exist is reported missing`() =
        runTest {
            coEvery { repository.getDeviceById(7) } returns null

            val viewModel = createViewModel(deviceId = 7)
            assertFalse(viewModel.uiState.value.isDeviceMissing)
            dispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isDeviceMissing)
        }

    @Test
    fun `removeModule delegates to repository`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.removeModule(moduleA)
            dispatcher.scheduler.advanceUntilIdle()

            coVerify { repository.selectModuleToBuy(moduleA) }
        }

    @Test
    fun `goBack emits a navigation command`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.goBack()

            assertEquals(IssuedCommand(Back, origin = BuyModule(1)), channelNavigator.commands.first())
        }
}
