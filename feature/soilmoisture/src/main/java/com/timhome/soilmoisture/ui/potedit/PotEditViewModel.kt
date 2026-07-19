package com.timhome.soilmoisture.ui.potedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.timhome.core.common.NavigationDispatcher
import com.timhome.core.common.di.IoDispatcher
import com.timhome.core.common.navigation.PotEdit
import com.timhome.core.data.repository.SoilMoistureRepository
import com.timhome.core.database.entity.PotEntity
import com.timhome.core.ui.viewmodel.ViewModelAssistedFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class PotEditUiState(
    val name: String = "",
    val channel: String = "",
    val isExistingPot: Boolean = false,
)

internal class PotEditViewModel
    @AssistedInject
    constructor(
        private val repository: SoilMoistureRepository,
        private val navigationDispatcher: NavigationDispatcher,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val route = savedStateHandle.toRoute<PotEdit>()
        private val roomId: Int = route.roomId
        private val potId: Int? = route.potId

        private val _uiState = MutableStateFlow(PotEditUiState(isExistingPot = potId != null))
        val uiState: StateFlow<PotEditUiState> = _uiState.asStateFlow()

        init {
            val id = potId
            if (id != null) {
                viewModelScope.launch(ioDispatcher) {
                    val pot = repository.getPot(id) ?: return@launch
                    _uiState.update { it.copy(name = pot.name, channel = pot.channel.toString()) }
                }
            }
        }

        fun onNameChanged(value: String) = _uiState.update { it.copy(name = value) }

        fun onChannelChanged(value: String) {
            if (value.isEmpty() || value.all { it.isDigit() }) {
                _uiState.update { it.copy(channel = value) }
            }
        }

        fun onSaveClick() {
            viewModelScope.launch(ioDispatcher) {
                val state = _uiState.value
                repository.savePot(
                    PotEntity(
                        id = potId ?: 0,
                        roomId = roomId,
                        name = state.name,
                        channel = state.channel.toIntOrNull() ?: 0,
                    ),
                )
                goBack()
            }
        }

        fun onDeleteClick() {
            val id = potId ?: return
            viewModelScope.launch(ioDispatcher) {
                val pot = repository.getPot(id) ?: return@launch
                repository.deletePot(pot)
                goBack()
            }
        }

        fun goBack() = navigationDispatcher.emit { it.popBackStack() }

        @AssistedFactory
        interface Factory : ViewModelAssistedFactory<PotEditViewModel> {
            override fun create(handle: SavedStateHandle): PotEditViewModel
        }
    }
