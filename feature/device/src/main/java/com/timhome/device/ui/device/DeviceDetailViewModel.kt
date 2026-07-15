package com.timhome.device.ui.device

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timhome.core.common.NavigationDispatcher
import com.timhome.core.ui.viewmodel.ViewModelAssistedFactory
import com.timhome.device.data.model.ModuleModel
import com.timhome.device.domain.IDeviceRepository
import com.timhome.device.domain.models.DeviceModel
import com.timhome.device.ui.listDevice.SELECTED_DEVICE_ID
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class DeviceDetailViewModel
    @AssistedInject
    constructor(
        @Assisted private val savedStateHandle: SavedStateHandle,
        private val navigationDispatcher: NavigationDispatcher,
        private val repository: IDeviceRepository,
    ) : ViewModel() {
        private val deviceId: Int? = savedStateHandle.get<Int>(SELECTED_DEVICE_ID)
        val device = MutableStateFlow<DeviceModel?>(null)
        val module = MutableStateFlow(listOf<ModuleModel>().toImmutableList())

        init {
            if (deviceId != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    getSelectedDeviceById(deviceId)
                }
            }
        }

        private suspend fun getSelectedDeviceById(deviceId: Int) {
            device.update {
                repository.getDeviceById(deviceId)
            }
            repository.getModuleToBuyByDeviceId(deviceId).stateIn(viewModelScope).collect { modules ->
                module.update {
                    modules.toImmutableList()
                }
            }
        }

        fun selectModuleToBuy(item: ModuleModel) {
            viewModelScope.launch {
                repository.selectModuleToBuy(item)
            }
        }

        fun buyModules() {
            navigationDispatcher.emit {
                it.navigate("buyModuleScreen/$deviceId")
            }
        }

        fun goBack() = navigationDispatcher.emit { it.popBackStack() }

        @AssistedFactory
        interface Factory : ViewModelAssistedFactory<DeviceDetailViewModel> {
            override fun create(handle: SavedStateHandle): DeviceDetailViewModel
        }
    }
