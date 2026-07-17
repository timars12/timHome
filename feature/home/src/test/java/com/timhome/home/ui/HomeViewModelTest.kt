package com.timhome.home.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.timhome.core.common.CallStatus
import com.timhome.core.database.entity.CarbonDioxideEntity
import com.timhome.core.data.repository.ArduinoRepository
import com.timhome.core.network.api.response.ArduinoResponse
import com.timhome.home.data.api.response.MainTemperature
import com.timhome.home.data.api.response.WeatherResponse
import com.timhome.home.data.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository: ArduinoRepository = mockk(relaxed = true)
    private val weatherRepository: WeatherRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    private fun createViewModel() =
        HomeViewModel(repository, weatherRepository, dispatcher, SavedStateHandle())

    @Test
    fun `populates ui state from successful calls`() =
        runTest {
            coEvery { repository.getCo2AndTemperature() } returns
                CallStatus.Success(ArduinoResponse(temperature = 21.6, co2 = 800))
            coEvery { weatherRepository.getWeather() } returns
                CallStatus.Success(WeatherResponse(MainTemperature(temp = 14.4, pressure = 1000)))
            every { repository.getCO2ValuesFromDB() } returns
                flowOf(listOf(CarbonDioxideEntity(co2Level = 1, date = "a"), CarbonDioxideEntity(co2Level = 2, date = "b")))

            val viewModel = createViewModel()
            dispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(22, state.temperatureInside)
            assertEquals(800, state.co2)
            assertEquals(14, state.temperatureOutside)
            assertEquals(false, state.isRefreshing)
            // reversed order from the DB flow
            assertEquals(2, state.measureCO2Levels.first().co2Level)
        }

    @Test
    fun `keeps defaults and logs when calls fail`() =
        runTest {
            coEvery { repository.getCo2AndTemperature() } returns CallStatus.Error("boom")
            coEvery { weatherRepository.getWeather() } returns CallStatus.Error("boom")
            every { repository.getCO2ValuesFromDB() } returns flowOf(emptyList())

            val viewModel = createViewModel()
            dispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(0, state.temperatureInside)
            assertEquals(0, state.co2)
            assertEquals(0, state.temperatureOutside)
            assertEquals(false, state.isRefreshing)
            assertEquals(0, state.measureCO2Levels.size)
        }
}
