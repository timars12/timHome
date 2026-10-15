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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

private const val STOP_TIMEOUT_MILLIS = 5000L

internal data class BuyModuleUiState(
    val totalPrice: BigDecimal = BigDecimal.ZERO,
    val modules: ImmutableList<ModuleModel> = persistentListOf(),
    /** The routed device does not exist. */
    val isDeviceMissing: Boolean = false,
)

internal class BuyModuleViewModel
    @AssistedInject
    constructor(
        @Assisted private val savedStateHandle: SavedStateHandle,
        @Assisted private val route: BuyModule,
        @Assisted private val navigator: Navigator,
        private val repository: IDeviceRepository,
    ) : ViewModel() {
        val uiState: StateFlow<BuyModuleUiState> =
            combine(
                flow { emit(repository.getDeviceById(route.deviceId) == null) },
                repository.getSelectedModuleToBuyByDeviceId(route.deviceId),
            ) { isDeviceMissing, modules ->
                BuyModuleUiState(
                    totalPrice = modules.sumOf { module -> BigDecimal(module.price.replace("$", "")) },
                    modules = modules.toImmutableList(),
                    isDeviceMissing = isDeviceMissing,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = BuyModuleUiState(),
            )

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
