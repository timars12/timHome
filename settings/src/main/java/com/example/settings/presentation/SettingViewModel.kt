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

class SettingViewModel @AssistedInject constructor(
    private val repository: SettingRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val ipAddress = savedStateHandle.getStateFlow("ipHomeAddress", "")

    init {
        viewModelScope.launch {
            savedStateHandle["ipHomeAddress"] = repository.getHomeIpAddress() ?: ""
        }
    }

    fun onIpAddressEntered(value: String) {
        savedStateHandle["ipHomeAddress"] = value
    }

    fun onSaveChangClick() {
        viewModelScope.launch {
            repository.setHomeIpAddress(ipAddress.value)
        }
    }

    @AssistedFactory
    interface Factory : ViewModelAssistedFactory<SettingViewModel> {
        override fun create(handle: SavedStateHandle): SettingViewModel
    }
}
