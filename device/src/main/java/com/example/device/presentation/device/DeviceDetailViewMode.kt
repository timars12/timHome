package com.example.device.presentation.device

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class DeviceDetailViewMode @AssistedInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val list = listOf(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14)

    @AssistedFactory
    interface Factory : ViewModelAssistedFactory<DeviceDetailViewMode> {
        override fun create(handle: SavedStateHandle): DeviceDetailViewMode
    }
}