package com.example.settings.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.repository.SettingRepository
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

private const val IP_HOME_ADDRESS_KEY = "ipHomeAddress"
private const val IS_USE_MOCK_KEY = "isUseMock"

internal class SettingViewModel
    @AssistedInject
    constructor(
        private val repository: SettingRepository,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        val ipAddress = savedStateHandle.getStateFlow(IP_HOME_ADDRESS_KEY, "")
        val isUseMock = savedStateHandle.getStateFlow(IS_USE_MOCK_KEY, true)

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
                repository.setHomeIpAddress(ipAddress.value)
                repository.setUseMock(isUseMock.value)
            }
        }

        @AssistedFactory
        interface Factory : ViewModelAssistedFactory<SettingViewModel> {
            override fun create(handle: SavedStateHandle): SettingViewModel
        }
    }
