package com.example.settings.presentation

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

class SettingViewModel @AssistedInject constructor(
    private val repository: SettingRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val ipAddress = savedStateHandle.getStateFlow(IP_HOME_ADDRESS_KEY, "")
    val isUseMock = savedStateHandle.getStateFlow(IS_USE_MOCK_KEY, true)

    init {
        viewModelScope.launch {
            savedStateHandle["ipHomeAddress"] = repository.getHomeIpAddress() ?: ""
            savedStateHandle["isUseMock"] = repository.checkIsUseMock()
        }
    }

    fun onIpAddressEntered(value: String) {
        savedStateHandle["ipHomeAddress"] = value
    }

    fun onSetUseMockClick(isUseMock: Boolean) {
        savedStateHandle["isUseMock"] = isUseMock
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
