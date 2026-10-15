package com.timhome.device.ui.device

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timhome.core.common.di.IoDispatcher
import com.timhome.core.common.navigation.BuyModule
import com.timhome.core.common.navigation.DeviceDetail
import com.timhome.core.common.navigation.Navigator
import com.timhome.core.common.navigation.Route
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5000L

internal data class DeviceDetailUiState(
    val device: DeviceModel? = null,
    val modules: ImmutableList<ModuleModel> = persistentListOf(),
    /** The routed device does not exist. While `false` with no [device], it is still loading. */
    val isDeviceMissing: Boolean = false,
)

internal class DeviceDetailViewModel
    @AssistedInject
    constructor(
        @Assisted private val savedStateHandle: SavedStateHandle,
        @Assisted private val route: DeviceDetail,
        @Assisted private val navigator: Navigator,
        private val repository: IDeviceRepository,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val deviceId: Int = route.deviceId

        val uiState: StateFlow<DeviceDetailUiState> =
            flow {
                val device = repository.getDeviceById(deviceId)
                if (device == null) {
                    emit(DeviceDetailUiState(isDeviceMissing = true))
                    return@flow
                }
                // Show the device while its modules load.
                emit(DeviceDetailUiState(device = device))
                emitAll(
                    repository.getModuleToBuyByDeviceId(deviceId).map { modules ->
                        DeviceDetailUiState(device = device, modules = modules.toImmutableList())
                    },
                )
            }.flowOn(ioDispatcher)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue = DeviceDetailUiState(),
                )

        fun selectModuleToBuy(item: ModuleModel) {
            viewModelScope.launch {
                repository.selectModuleToBuy(item)
            }
        }

        fun buyModules() {
            navigator.navigate(BuyModule(deviceId))
        }

        fun goBack() = navigator.back()

        @AssistedFactory
        interface Factory : ViewModelAssistedFactory<DeviceDetailViewModel> {
            fun createFor(
                handle: SavedStateHandle,
                route: DeviceDetail,
                navigator: Navigator,
            ): DeviceDetailViewModel

            override fun create(
                handle: SavedStateHandle,
                route: Route,
                navigator: Navigator,
            ): DeviceDetailViewModel = createFor(handle, route as DeviceDetail, navigator)

            override fun create(handle: SavedStateHandle): DeviceDetailViewModel = error("needs a route and a navigator")
        }
    }
