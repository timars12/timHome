package com.timhome.device.ui.listDevice

import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timhome.core.common.di.IoDispatcher
import com.timhome.core.common.navigation.DeviceDetail
import com.timhome.core.common.navigation.Navigator
import com.timhome.core.common.navigation.Route
import com.timhome.core.ui.viewmodel.ViewModelAssistedFactory
import com.timhome.device.domain.IDeviceRepository
import com.timhome.device.domain.models.DeviceModel
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MILLIS = 5000L

internal data class DeviceListUiState(
    val devices: ImmutableList<DeviceModel> = persistentListOf(),
)

internal class DeviceListViewModel
    @AssistedInject
    constructor(
        @Assisted private val savedStateHandle: SavedStateHandle,
        @Assisted private val navigator: Navigator,
        private val repository: IDeviceRepository,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        firebaseAnalytics: FirebaseAnalytics,
    ) : ViewModel() {
        val uiState: StateFlow<DeviceListUiState> =
            repository.getAllDevices()
                .onEach { list -> if (list.isEmpty()) repository.initAllDevices() }
                .map { list -> DeviceListUiState(devices = list.toImmutableList()) }
                .flowOn(ioDispatcher)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue = DeviceListUiState(),
                )

        init {
            firebaseAnalytics.logEvent(
                FirebaseAnalytics.Event.SCREEN_VIEW,
                bundleOf(Pair(FirebaseAnalytics.Param.SCREEN_NAME, "device_list")),
            )
        }

        fun navigateToDetailScreen(device: DeviceModel) {
            navigator.navigate(DeviceDetail(device.id))
        }

        @AssistedFactory
        interface Factory : ViewModelAssistedFactory<DeviceListViewModel> {
            fun createFor(
                handle: SavedStateHandle,
                navigator: Navigator,
            ): DeviceListViewModel

            override fun create(
                handle: SavedStateHandle,
                route: Route,
                navigator: Navigator,
            ): DeviceListViewModel = createFor(handle, navigator)

            override fun create(handle: SavedStateHandle): DeviceListViewModel = error("needs a route and a navigator")
        }
    }
