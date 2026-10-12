package com.timhome.device.ui.buyModule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timhome.core.common.navigation.BuyModule
import com.timhome.core.common.navigation.Navigator
import com.timhome.core.common.navigation.Route
import com.timhome.core.ui.viewmodel.ViewModelAssistedFactory
import com.timhome.device.data.model.ModuleModel
import com.timhome.device.domain.IDeviceRepository
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
        @Assisted private val route: BuyModule,
        @Assisted private val navigator: Navigator,
        private val repository: IDeviceRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(BuyModuleUiState())
        val uiState: StateFlow<BuyModuleUiState> = _uiState.asStateFlow()

        init {
            val selectedModules = repository.getSelectedModuleToBuyByDeviceId(route.deviceId)

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
            navigator.back()
        }

        @AssistedFactory
        interface Factory : ViewModelAssistedFactory<BuyModuleViewModel> {
            fun createFor(
                handle: SavedStateHandle,
                route: BuyModule,
                navigator: Navigator,
            ): BuyModuleViewModel

            override fun create(
                handle: SavedStateHandle,
                route: Route,
                navigator: Navigator,
            ): BuyModuleViewModel = createFor(handle, route as BuyModule, navigator)

            override fun create(handle: SavedStateHandle): BuyModuleViewModel = error("needs a route and a navigator")
        }
    }
