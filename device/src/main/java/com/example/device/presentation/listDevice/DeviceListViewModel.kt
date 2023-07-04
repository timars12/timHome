package com.example.device.presentation.listDevice

import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.utils.NavigationDispatcher
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import com.example.device.R
import com.example.device.data.repository.DeviceRepository
import com.example.device.domain.models.DeviceModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class DeviceListViewModel @AssistedInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val navigationDispatcher: NavigationDispatcher,
    private val repository: DeviceRepository
) : ViewModel() {
    val deviceList = MutableStateFlow(listOf<DeviceModel>().toImmutableList())

    init {
        viewModelScope.launch(Dispatchers.IO) { repository.initAllDevices() }
    }

    fun getAllDevices() = repository.getAllDevices()

    fun updateList(list: ImmutableList<DeviceModel>) = deviceList.update { list }

    fun navigateToDetailScreen(device: DeviceModel) {
        navigationDispatcher.emit {
            it.navigate(
                R.id.deviceDetailFragment,
                bundleOf(SELECTED_DEVICE_ID to device.id)
            )
        }
    }

    @AssistedFactory
    interface Factory : ViewModelAssistedFactory<DeviceListViewModel> {
        override fun create(handle: SavedStateHandle): DeviceListViewModel
    }
}
