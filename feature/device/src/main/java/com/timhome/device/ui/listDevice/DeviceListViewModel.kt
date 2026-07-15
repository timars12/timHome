package com.timhome.device.ui.listDevice

import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timhome.core.common.NavigationDispatcher
import com.timhome.core.utils.viewmodel.ViewModelAssistedFactory
import com.timhome.device.data.repository.DeviceRepository
import com.timhome.device.domain.models.DeviceModel
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class DeviceListViewModel
    @AssistedInject
    constructor(
        @Assisted private val savedStateHandle: SavedStateHandle,
        private val navigationDispatcher: NavigationDispatcher,
        private val repository: DeviceRepository,
        firebaseAnalytics: FirebaseAnalytics,
    ) : ViewModel() {
        val deviceList = MutableStateFlow(listOf<DeviceModel>().toImmutableList())

        init {
            firebaseAnalytics.logEvent(
                FirebaseAnalytics.Event.SCREEN_VIEW,
                bundleOf(Pair(FirebaseAnalytics.Param.SCREEN_NAME, "device_list")),
            )
            viewModelScope.launch(Dispatchers.IO) {
                repository.getAllDevices().stateIn(viewModelScope).collect { list ->
                    if (list.isEmpty()) repository.initAllDevices()
                    deviceList.update { list }
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
