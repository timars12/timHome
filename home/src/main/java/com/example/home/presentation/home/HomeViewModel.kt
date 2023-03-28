package com.example.home.presentation.home

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.db.entity.CarbonDioxideEntity
import com.example.core.data.repository.ArduinoRepository
import com.example.core.utils.CallStatus
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import com.example.home.data.repository.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class HomeViewModel @AssistedInject constructor(
    private val repository: ArduinoRepository,
    private val weatherRepository: WeatherRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val co2 = MutableStateFlow(0)
    val temperatureInside = MutableStateFlow(0)
    val temperatureOutside = MutableStateFlow(0)
    val isRefreshing = MutableStateFlow(false)

    val measureCO2Levels = MutableStateFlow(listOf<CarbonDioxideEntity>().toImmutableList())

    init {
        getDate()
    }

    fun getDate() {
        viewModelScope.launch {
            isRefreshing.value = true
            val arduino = async { getCO2AndTemperature() }
            val webTemp = async { getTemperatureOutSide() }
            val co2 = async { getCO2ValuesFromDB() }
            awaitAll(arduino, webTemp, co2)
            isRefreshing.value = false
        }
    }

    private suspend fun getCO2AndTemperature() {
        when (val result = repository.getCo2AndTemperature()) {
            is CallStatus.Success -> {
                temperatureInside.value = result.data?.temperature?.roundToInt() ?: 0
                co2.value = result.data?.co2 ?: 0
            }
            is CallStatus.Error -> {
                Log.e("0909", "getCO2AndTemperature") // TODO
            }
        }
    }

    private suspend fun getTemperatureOutSide() {
        when (val result = weatherRepository.getWeather()) {
            is CallStatus.Success -> {
                temperatureOutside.value = result.data?.main?.temp?.roundToInt() ?: 0
            }
            is CallStatus.Error -> {
                Log.e("0909", "getTemperatureOutSide") // TODO
            }
        }
    }

    private suspend fun getCO2ValuesFromDB() {
        repository.getCO2ValuesFromDB().collect { list ->
            measureCO2Levels.update { list.toImmutableList() }
        }
    }

    @AssistedFactory
    interface Factory : ViewModelAssistedFactory<HomeViewModel> {
        override fun create(handle: SavedStateHandle): HomeViewModel
    }
}
