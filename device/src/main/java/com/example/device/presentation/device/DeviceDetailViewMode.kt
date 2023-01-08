package com.example.device.presentation.device

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import com.example.device.domain.IDeviceRepository
import com.example.device.presentation.listDevice.SELECTED_DEVICE_ID
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeviceDetailViewMode @AssistedInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val repository: IDeviceRepository
) : ViewModel() {
    val list = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)

    init {
        savedStateHandle.get<Int>(SELECTED_DEVICE_ID)?.let {
            getSelectedDeviceById(it)
        }
    }

    private fun getSelectedDeviceById(deviceId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val device = repository.getSelectedDeviceById(deviceId)
            println(device)
        }
    }

    @AssistedFactory
    interface Factory : ViewModelAssistedFactory<DeviceDetailViewMode> {
        override fun create(handle: SavedStateHandle): DeviceDetailViewMode
    }
}
