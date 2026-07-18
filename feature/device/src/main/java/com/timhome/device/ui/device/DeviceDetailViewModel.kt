package com.timhome.device.ui.device

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.timhome.core.common.NavigationDispatcher
import com.timhome.core.common.di.IoDispatcher
import com.timhome.core.common.navigation.BuyModule
import com.timhome.core.common.navigation.DeviceDetail
import com.timhome.core.ui.viewmodel.ViewModelAssistedFactory
import com.timhome.device.data.model.ModuleModel
import com.timhome.device.domain.IDeviceRepository
import com.timhome.device.domain.models.DeviceModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class DeviceDetailUiState(
    val device: DeviceModel? = null,
    val modules: ImmutableList<ModuleModel> = persistentListOf(),
)

internal class DeviceDetailViewModel
    @AssistedInject
    constructor(
        @Assisted private val savedStateHandle: SavedStateHandle,
        private val navigationDispatcher: NavigationDispatcher,
        private val repository: IDeviceRepository,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val deviceId: Int = savedStateHandle.toRoute<DeviceDetail>().deviceId

        private val _uiState = MutableStateFlow(DeviceDetailUiState())
        val uiState: StateFlow<DeviceDetailUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch(ioDispatcher) {
                getSelectedDeviceById(deviceId)
            }
        }

        private suspend fun getSelectedDeviceById(deviceId: Int) {
            _uiState.update { it.copy(device = repository.getDeviceById(deviceId)) }
            repository.getModuleToBuyByDeviceId(deviceId).stateIn(viewModelScope).collect { modules ->
                _uiState.update { it.copy(modules = modules.toImmutableList()) }
            }
        }

        fun selectModuleToBuy(item: ModuleModel) {
            viewModelScope.launch {
                repository.selectModuleToBuy(item)
            }
        }

        fun buyModules() {
            navigationDispatcher.emit {
                it.navigate(BuyModule(deviceId))
            }
        }

        fun goBack() = navigationDispatcher.emit { it.popBackStack() }

        @AssistedFactory
        interface Factory : ViewModelAssistedFactory<DeviceDetailViewModel> {
            override fun create(handle: SavedStateHandle): DeviceDetailViewModel
        }
    }
