package com.example.device.presentation.device

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.utils.NavigationDispatcher
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import com.example.device.data.model.ModuleModel
import com.example.device.domain.IDeviceRepository
import com.example.device.domain.models.DeviceModel
import com.example.device.presentation.listDevice.SELECTED_DEVICE_ID
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceDetailViewMode @AssistedInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val navigationDispatcher: NavigationDispatcher,
    private val repository: IDeviceRepository
) : ViewModel() {
    val device = MutableStateFlow<DeviceModel?>(null)
    val module = MutableStateFlow<List<ModuleModel>>(listOf())

    init {
        savedStateHandle.get<Int>(SELECTED_DEVICE_ID)?.let {
            getSelectedDeviceById(it)
        }
    }

    private fun getSelectedDeviceById(deviceId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val model = repository.getDeviceWithModuleById(deviceId)
            device.value = model.device
            module.value = model.modules
        }
    }

    fun goBack() = navigationDispatcher.emit { it.popBackStack() }

    @AssistedFactory
    interface Factory : ViewModelAssistedFactory<DeviceDetailViewMode> {
        override fun create(handle: SavedStateHandle): DeviceDetailViewMode
    }
}
