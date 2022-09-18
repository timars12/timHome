package com.example.device.presentation.listDevice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class DeviceListViewModel @AssistedInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    @AssistedFactory
    interface Factory : ViewModelAssistedFactory<DeviceListViewModel> {
        override fun create(handle: SavedStateHandle): DeviceListViewModel
    }
}
