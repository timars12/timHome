package com.timhome.device.ui.buyModule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timhome.core.common.NavigationDispatcher
import com.timhome.core.ui.viewmodel.ViewModelAssistedFactory
import com.timhome.device.data.model.ModuleModel
import com.timhome.device.domain.IDeviceRepository
import com.timhome.device.ui.listDevice.SELECTED_DEVICE_ID
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

internal data class BuyModuleUiState(
    val totalPrice: BigDecimal = BigDecimal.ZERO,
    val modules: ImmutableList<ModuleModel> = persistentListOf(),
)

internal class BuyModuleViewModel
    @AssistedInject
    constructor(
        @Assisted private val savedStateHandle: SavedStateHandle,
        private val navigationDispatcher: NavigationDispatcher,
        private val repository: IDeviceRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(BuyModuleUiState())
        val uiState: StateFlow<BuyModuleUiState> = _uiState.asStateFlow()

        init {
            val deviceId = savedStateHandle.get<Int>(SELECTED_DEVICE_ID)
            val selectedModules =
                when {
                    deviceId != null -> repository.getSelectedModuleToBuyByDeviceId(deviceId)
                    else -> repository.getSelectedModuleToBuy()
                }

            viewModelScope.launch {
                selectedModules.stateIn(viewModelScope).collect { list ->
                    _uiState.update {
                        it.copy(
                            totalPrice = list.sumOf { module -> BigDecimal(module.price.replace("$", "")) },
                            modules = list.toImmutableList(),
                        )
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
