package com.timhome.device.ui.listDevice

import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timhome.core.common.NavigationDispatcher
import com.timhome.core.common.di.IoDispatcher
import com.timhome.core.ui.viewmodel.ViewModelAssistedFactory
import com.timhome.device.data.repository.DeviceRepository
import com.timhome.device.domain.models.DeviceModel
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class DeviceListUiState(
    val devices: ImmutableList<DeviceModel> = persistentListOf(),
)

internal class DeviceListViewModel
    @AssistedInject
    constructor(
        @Assisted private val savedStateHandle: SavedStateHandle,
        private val navigationDispatcher: NavigationDispatcher,
        private val repository: DeviceRepository,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        firebaseAnalytics: FirebaseAnalytics,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(DeviceListUiState())
        val uiState: StateFlow<DeviceListUiState> = _uiState.asStateFlow()

        init {
            firebaseAnalytics.logEvent(
                FirebaseAnalytics.Event.SCREEN_VIEW,
                bundleOf(Pair(FirebaseAnalytics.Param.SCREEN_NAME, "device_list")),
            )
            viewModelScope.launch(ioDispatcher) {
                repository.getAllDevices().stateIn(viewModelScope).collect { list ->
                    if (list.isEmpty()) repository.initAllDevices()
                    _uiState.update { it.copy(devices = list) }
                }
            }
        }

        fun navigateToDetailScreen(device: DeviceModel) {
            navigationDispatcher.emit {
                it.navigate("deviceDetailScreen/${device.id}")
            }
        }

        @AssistedFactory
        interface Factory : ViewModelAssistedFactory<DeviceListViewModel> {
            override fun create(handle: SavedStateHandle): DeviceListViewModel
        }
    }
