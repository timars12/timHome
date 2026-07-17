package com.timhome.home.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timhome.core.database.entity.CarbonDioxideEntity
import com.timhome.core.data.repository.ArduinoRepository
import com.timhome.core.common.CallStatus
import com.timhome.core.common.di.IoDispatcher
import com.timhome.core.ui.viewmodel.ViewModelAssistedFactory
import com.timhome.home.data.repository.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class HomeUiState(
    val co2: Int = 0,
    val temperatureInside: Int = 0,
    val temperatureOutside: Int = 0,
    val isRefreshing: Boolean = false,
    val measureCO2Levels: ImmutableList<CarbonDioxideEntity> = persistentListOf(),
)

internal class HomeViewModel
    @AssistedInject
    constructor(
        private val repository: ArduinoRepository,
        private val weatherRepository: WeatherRepository,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HomeUiState())
        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

        init {
            getDate()
        }

        private fun getDate() {
            viewModelScope.launch(ioDispatcher) {
                _uiState.update { it.copy(isRefreshing = true) }
                val arduino = async { getCO2AndTemperature() }
                val webTemp = async { getTemperatureOutSide() }
                awaitAll(arduino, webTemp)
                _uiState.update { it.copy(isRefreshing = false) }
                getCO2ValuesFromDB()
            }
        }

        private suspend fun getCO2AndTemperature() {
            when (val result = repository.getCo2AndTemperature()) {
                is CallStatus.Success -> {
                    _uiState.update {
                        it.copy(
                            temperatureInside = result.data?.temperature?.roundToInt() ?: 0,
                            co2 = result.data?.co2 ?: 0,
                        )
                    }
                }
                is CallStatus.Error -> {
                    Log.e("0909", "getCO2AndTemperature") // TODO show error
                }
            }
        }

        private suspend fun getTemperatureOutSide() {
            when (val result = weatherRepository.getWeather()) {
                is CallStatus.Success -> {
                    _uiState.update {
                        it.copy(temperatureOutside = result.data?.main?.temp?.roundToInt() ?: 0)
                    }
                }
                is CallStatus.Error -> {
                    Log.e("0909", "getTemperatureOutSide") // TODO
                }
            }
        }

        private suspend fun getCO2ValuesFromDB() {
            repository.getCO2ValuesFromDB()
                .collectLatest { list ->
                    _uiState.update { it.copy(measureCO2Levels = list.reversed().toImmutableList()) }
                }
        }

        @AssistedFactory
        interface Factory : ViewModelAssistedFactory<HomeViewModel> {
            override fun create(handle: SavedStateHandle): HomeViewModel
        }
    }
