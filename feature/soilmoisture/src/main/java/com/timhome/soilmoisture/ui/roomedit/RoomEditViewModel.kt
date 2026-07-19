package com.timhome.soilmoisture.ui.roomedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.timhome.core.common.NavigationDispatcher
import com.timhome.core.common.di.IoDispatcher
import com.timhome.core.common.navigation.RoomEdit
import com.timhome.core.data.repository.SoilMoistureRepository
import com.timhome.core.database.entity.RoomEntity
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

internal data class RoomEditUiState(
    val name: String = "",
    val ipAddress: String = "",
    val isExistingRoom: Boolean = false,
)

internal class RoomEditViewModel
    @AssistedInject
    constructor(
        private val repository: SoilMoistureRepository,
        private val navigationDispatcher: NavigationDispatcher,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val roomId: Int? = savedStateHandle.toRoute<RoomEdit>().roomId

        private val _uiState = MutableStateFlow(RoomEditUiState(isExistingRoom = roomId != null))
        val uiState: StateFlow<RoomEditUiState> = _uiState.asStateFlow()

        init {
            val id = roomId
            if (id != null) {
                viewModelScope.launch(ioDispatcher) {
                    val room = repository.getRoom(id) ?: return@launch
                    _uiState.update { it.copy(name = room.name, ipAddress = room.ipAddress) }
                }
            }
        }

        fun onNameChanged(value: String) = _uiState.update { it.copy(name = value) }

        fun onIpAddressChanged(value: String) = _uiState.update { it.copy(ipAddress = value) }

        fun onSaveClick() {
            viewModelScope.launch(ioDispatcher) {
                val state = _uiState.value
                repository.saveRoom(
                    RoomEntity(id = roomId ?: 0, name = state.name, ipAddress = state.ipAddress),
                )
                goBack()
            }
        }

        fun onDeleteClick() {
            val id = roomId ?: return
            viewModelScope.launch(ioDispatcher) {
                val room = repository.getRoom(id) ?: return@launch
                repository.deleteRoom(room)
                goBack()
            }
        }

        fun goBack() = navigationDispatcher.emit { it.popBackStack() }

        @AssistedFactory
        interface Factory : ViewModelAssistedFactory<RoomEditViewModel> {
            override fun create(handle: SavedStateHandle): RoomEditViewModel
        }
    }
