package com.example.device.ui.buyModule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.utils.NavigationDispatcher
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import com.example.device.data.model.ModuleModel
import com.example.device.domain.IDeviceRepository
import com.example.device.ui.listDevice.SELECTED_DEVICE_ID
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

internal class BuyModuleViewModel
    @AssistedInject
    constructor(
        @Assisted private val savedStateHandle: SavedStateHandle,
        private val navigationDispatcher: NavigationDispatcher,
        private val repository: IDeviceRepository,
    ) : ViewModel() {
        val totalPrice = MutableStateFlow<BigDecimal>(BigDecimal.ZERO)
        val modules = MutableStateFlow(listOf<ModuleModel>().toImmutableList())

        init {
            val deviceId = savedStateHandle.get<Int>(SELECTED_DEVICE_ID)
            val selectedModules =
                when {
                    deviceId != null -> repository.getSelectedModuleToBuyByDeviceId(deviceId)
                    else -> repository.getSelectedModuleToBuy()
                }

            viewModelScope.launch {
                selectedModules.stateIn(viewModelScope).collect { list ->
                    totalPrice.update {
                        list.sumOf {
                            BigDecimal(it.price.replace("$", ""))
                        }
                    }
                    modules.update {
                        list.toImmutableList()
                    }
                }
            }
        }

        fun removeModule(item: ModuleModel) {
            viewModelScope.launch {
                repository.selectModuleToBuy(item)
            }
        }

        fun goBack() {
            navigationDispatcher.emit { it.popBackStack() }
        }

        @AssistedFactory
        interface Factory : ViewModelAssistedFactory<BuyModuleViewModel> {
            override fun create(handle: SavedStateHandle): BuyModuleViewModel
        }
    }
