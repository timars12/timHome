package com.timhome.settings.ui

import androidx.lifecycle.SavedStateHandle
import com.timhome.core.data.repository.SettingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SettingViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository: SettingRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SettingViewModel(repository, SavedStateHandle())

    @Test
    fun `loads persisted settings into ui state`() =
        runTest {
            coEvery { repository.getHomeIpAddress() } returns "192.168.0.1"
            every { repository.checkIsUseMock() } returns false

            val viewModel = createViewModel()
            val job = launch { viewModel.uiState.collect {} }
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals("192.168.0.1", viewModel.uiState.value.ipAddress)
            assertEquals(false, viewModel.uiState.value.isUseMock)

            job.cancel()
        }

    @Test
    fun `falls back to empty ip when repository returns null`() =
        runTest {
            coEvery { repository.getHomeIpAddress() } returns null
            every { repository.checkIsUseMock() } returns true

            val viewModel = createViewModel()
            val job = launch { viewModel.uiState.collect {} }
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals("", viewModel.uiState.value.ipAddress)
            assertEquals(true, viewModel.uiState.value.isUseMock)

            job.cancel()
        }

    @Test
    fun `onIpAddressEntered updates ui state`() =
        runTest {
            coEvery { repository.getHomeIpAddress() } returns ""
            every { repository.checkIsUseMock() } returns true

            val viewModel = createViewModel()
            val job = launch { viewModel.uiState.collect {} }
            dispatcher.scheduler.advanceUntilIdle()

            viewModel.onIpAddressEntered("10.0.0.5")
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals("10.0.0.5", viewModel.uiState.value.ipAddress)

            job.cancel()
        }

    @Test
    fun `onSetUseMockClick updates ui state`() =
        runTest {
            coEvery { repository.getHomeIpAddress() } returns ""
            every { repository.checkIsUseMock() } returns true

            val viewModel = createViewModel()
            val job = launch { viewModel.uiState.collect {} }
            dispatcher.scheduler.advanceUntilIdle()

            viewModel.onSetUseMockClick(false)
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(false, viewModel.uiState.value.isUseMock)

            job.cancel()
        }

    @Test
    fun `onSaveChangClick persists current values`() =
        runTest {
            coEvery { repository.getHomeIpAddress() } returns ""
            every { repository.checkIsUseMock() } returns true

            val viewModel = createViewModel()
            val job = launch { viewModel.uiState.collect {} }
            dispatcher.scheduler.advanceUntilIdle()

            viewModel.onIpAddressEntered("10.0.0.5")
            viewModel.onSetUseMockClick(false)
            viewModel.onSaveChangClick()
            dispatcher.scheduler.advanceUntilIdle()

            coVerify { repository.setHomeIpAddress("10.0.0.5") }
            coVerify { repository.setUseMock(false) }

            job.cancel()
        }
}
