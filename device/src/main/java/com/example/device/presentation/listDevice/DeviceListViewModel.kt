package com.example.device.presentation.listDevice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import com.example.device.data.repository.DeviceRepository
import com.example.device.domain.models.DeviceModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeviceListViewModel @AssistedInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val repository: DeviceRepository
) : ViewModel() {
    val deviceList = MutableStateFlow(listOf<DeviceModel>())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllDevices().collect { list ->
                deviceList.update { list }
            }
        }
    }
    @AssistedFactory
    interface Factory : ViewModelAssistedFactory<DeviceListViewModel> {
        override fun create(handle: SavedStateHandle): DeviceListViewModel
    }
}
