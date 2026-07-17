package com.timhome.device.ui.buyModule

import androidx.lifecycle.SavedStateHandle
import com.timhome.core.common.NavigationDispatcher
import com.timhome.device.data.model.ModuleModel
import com.timhome.device.domain.IDeviceRepository
import com.timhome.device.ui.listDevice.SELECTED_DEVICE_ID
import io.mockk.coVerify
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
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class BuyModuleViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository: IDeviceRepository = mockk(relaxed = true)
    private val navigationDispatcher = NavigationDispatcher()

    private val moduleA =
        ModuleModel(id = 1, title = "A", price = "$3", link = "l", isSelectToBuy = true)
    private val moduleB =
        ModuleModel(id = 2, title = "B", price = "$4", link = "l", isSelectToBuy = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(deviceId: Int?): BuyModuleViewModel {
        val handle = SavedStateHandle()
        if (deviceId != null) handle[SELECTED_DEVICE_ID] = deviceId
        return BuyModuleViewModel(handle, navigationDispatcher, repository)
    }

    @Test
    fun `sums prices and exposes modules for a device`() =
        runTest {
            every { repository.getSelectedModuleToBuyByDeviceId(7) } returns flowOf(listOf(moduleA, moduleB))

            val viewModel = createViewModel(deviceId = 7)
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(BigDecimal(7), viewModel.uiState.value.totalPrice)
            assertEquals(listOf(moduleA, moduleB), viewModel.uiState.value.modules)
        }

    @Test
    fun `falls back to global selection when no deviceId`() =
        runTest {
            every { repository.getSelectedModuleToBuy() } returns flowOf(listOf(moduleA))

            val viewModel = createViewModel(deviceId = null)
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(BigDecimal(3), viewModel.uiState.value.totalPrice)
            assertEquals(listOf(moduleA), viewModel.uiState.value.modules)
        }

    @Test
    fun `removeModule delegates to repository`() =
        runTest {
            every { repository.getSelectedModuleToBuy() } returns flowOf(emptyList())

            val viewModel = createViewModel(deviceId = null)

            viewModel.removeModule(moduleA)
            dispatcher.scheduler.advanceUntilIdle()

            coVerify { repository.selectModuleToBuy(moduleA) }
        }

    @Test
    fun `goBack emits a navigation command`() =
        runTest {
            every { repository.getSelectedModuleToBuy() } returns flowOf(emptyList())

            val viewModel = createViewModel(deviceId = null)

            viewModel.goBack()

            assertNotNull(navigationDispatcher.navigationEmitter.tryReceive().getOrNull())
        }
}
