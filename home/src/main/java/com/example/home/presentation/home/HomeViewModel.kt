package com.example.home.presentation.home

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.repository.ArduinoRepository
import com.example.core.utils.CallStatus
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HomeViewModel @AssistedInject constructor(
    private val repository: ArduinoRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val co2 = MutableStateFlow(0)
    val temperatureInside = MutableStateFlow(0)
    val isRefreshing = MutableStateFlow(false)

    init {
        getCO2AndTemperature()
    }

    fun getCO2AndTemperature() {
        viewModelScope.launch {
            isRefreshing.value = true
            when (val result = repository.getCo2AndTemperature()) {
                is CallStatus.Success -> {
                    temperatureInside.value = result.data?.temperature?.toInt() ?: 0
                    co2.value = result.data?.co2 ?: 0
                    isRefreshing.value = false
                }
                is CallStatus.Error -> {
                    isRefreshing.value = false
                    Log.e("0909", "scxc")
                }
            }
        }
    }

    @AssistedFactory
    interface Factory : ViewModelAssistedFactory<HomeViewModel> {
        override fun create(handle: SavedStateHandle): HomeViewModel
    }
}