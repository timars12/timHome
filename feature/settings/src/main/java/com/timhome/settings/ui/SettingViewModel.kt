package com.timhome.settings.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timhome.core.data.repository.SettingRepository
import com.timhome.core.ui.viewmodel.ViewModelAssistedFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val IP_HOME_ADDRESS_KEY = "ipHomeAddress"
private const val IS_USE_MOCK_KEY = "isUseMock"
private const val STOP_TIMEOUT_MILLIS = 5000L

internal data class SettingUiState(
    val ipAddress: String = "",
    val isUseMock: Boolean = true,
)

internal class SettingViewModel
    @AssistedInject
    constructor(
        private val repository: SettingRepository,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val ipAddressFlow = savedStateHandle.getStateFlow(IP_HOME_ADDRESS_KEY, "")
        private val isUseMockFlow = savedStateHandle.getStateFlow(IS_USE_MOCK_KEY, true)

        val uiState: StateFlow<SettingUiState> =
            combine(ipAddressFlow, isUseMockFlow) { ipAddress, isUseMock ->
                SettingUiState(ipAddress = ipAddress, isUseMock = isUseMock)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = SettingUiState(),
            )

        init {
            viewModelScope.launch {
                savedStateHandle[IP_HOME_ADDRESS_KEY] = repository.getHomeIpAddress() ?: ""
                savedStateHandle[IS_USE_MOCK_KEY] = repository.checkIsUseMock()
            }
        }

        fun onIpAddressEntered(value: String) {
            savedStateHandle[IP_HOME_ADDRESS_KEY] = value
        }

        fun onSetUseMockClick(isUseMock: Boolean) {
            savedStateHandle[IS_USE_MOCK_KEY] = isUseMock
        }

        fun onSaveChangClick() {
            viewModelScope.launch {
                repository.setHomeIpAddress(ipAddressFlow.value)
                repository.setUseMock(isUseMockFlow.value)
            }
        }

        @AssistedFactory
        interface Factory : ViewModelAssistedFactory<SettingViewModel> {
            override fun create(handle: SavedStateHandle): SettingViewModel
        }
    }
