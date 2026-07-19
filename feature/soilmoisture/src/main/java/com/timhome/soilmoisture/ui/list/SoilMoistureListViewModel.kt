package com.timhome.soilmoisture.ui.list

import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timhome.core.common.CallStatus
import com.timhome.core.common.NavigationDispatcher
import com.timhome.core.common.WateringRecheckScheduler
import com.timhome.core.common.di.IoDispatcher
import com.timhome.core.common.navigation.PotEdit
import com.timhome.core.common.navigation.RoomEdit
import com.timhome.core.data.repository.SoilMoistureRepository
import com.timhome.core.database.entity.PotEntity
import com.timhome.core.database.entity.RoomEntity
import com.timhome.core.ui.viewmodel.ViewModelAssistedFactory
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class SoilMoistureUiState(
    val rooms: ImmutableList<RoomUiModel> = persistentListOf(),
    val isLoading: Boolean = true,
    val snackbarMessage: String? = null,
)

internal data class RoomUiModel(
    val id: Int,
    val name: String,
    val temperature: Double?,
    val humidity: Double?,
    val isConnected: Boolean,
    val connectionError: String?,
    val pots: ImmutableList<PotUiModel>,
)

internal data class PotUiModel(
    val id: Int,
    val roomId: Int,
    val name: String,
    val moisturePercent: Int?,
    val lastWateredAt: String?,
    val hasWaterAlarm: Boolean,
)

internal class SoilMoistureListViewModel
    @AssistedInject
    constructor(
        private val repository: SoilMoistureRepository,
        private val navigationDispatcher: NavigationDispatcher,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        @Assisted private val savedStateHandle: SavedStateHandle,
        firebaseAnalytics: FirebaseAnalytics,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SoilMoistureUiState())
        val uiState: StateFlow<SoilMoistureUiState> = _uiState.asStateFlow()

        init {
            firebaseAnalytics.logEvent(
                FirebaseAnalytics.Event.SCREEN_VIEW,
                bundleOf(Pair(FirebaseAnalytics.Param.SCREEN_NAME, "soil_moisture")),
            )
            observeRooms()
        }

        private fun observeRooms() {
            viewModelScope.launch(ioDispatcher) {
                combine(repository.getRooms(), repository.getPots()) { rooms, pots -> rooms to pots }
                    .flatMapLatest { (rooms, pots) -> buildRoomModels(rooms, pots) }
                    .collect { roomModels ->
                        _uiState.update { it.copy(rooms = roomModels.toImmutableList(), isLoading = false) }
                    }
            }
        }

        private fun buildRoomModels(
            rooms: List<RoomEntity>,
            pots: List<PotEntity>,
        ): Flow<List<RoomUiModel>> {
            if (rooms.isEmpty()) return flowOf(emptyList())
            val roomFlows =
                rooms.map { room ->
                    val roomPots = pots.filter { it.roomId == room.id }
                    combine(repository.getLatestClimate(room.id), buildPotModels(roomPots)) { climate, potModels ->
                        RoomUiModel(
                            id = room.id,
                            name = room.name,
                            temperature = climate?.temperature,
                            humidity = climate?.humidity,
                            isConnected = room.lastPollSuccessful,
                            connectionError = room.lastPollError,
                            pots = potModels.toImmutableList(),
                        )
                    }
                }
            return combine(roomFlows) { it.toList() }
        }

        private fun buildPotModels(pots: List<PotEntity>): Flow<List<PotUiModel>> {
            if (pots.isEmpty()) return flowOf(emptyList())
            val potFlows =
                pots.map { pot ->
                    combine(
                        repository.getLatestMoisture(pot.id),
                        repository.getLatestWatering(pot.id),
                    ) { moisture, watering ->
                        PotUiModel(
                            id = pot.id,
                            roomId = pot.roomId,
                            name = pot.name,
                            moisturePercent = moisture?.moisturePercent,
                            lastWateredAt = watering?.wateredAt,
                            hasWaterAlarm = pot.alarmActive,
                        )
                    }
                }
            return combine(potFlows) { it.toList() }
        }

        fun waterPot(potId: Int) {
            viewModelScope.launch(ioDispatcher) {
                val pot = repository.getPot(potId) ?: return@launch
                val room = repository.getRoom(pot.roomId) ?: return@launch
                when (val result = repository.waterPot(pot, room)) {
                    is CallStatus.Error -> {
                        val message = result.error ?: "Не вдалося звʼязатися з ESP32"
                        _uiState.update { it.copy(snackbarMessage = message) }
                    }
                    is CallStatus.Success -> WateringRecheckScheduler.instance.scheduleQuickRecheck()
                }
            }
        }

        fun consumeSnackbarMessage() {
            _uiState.update { it.copy(snackbarMessage = null) }
        }

        fun navigateToAddRoom() {
            navigationDispatcher.emit { it.navigate(RoomEdit()) }
        }

        fun navigateToEditRoom(roomId: Int) {
            navigationDispatcher.emit { it.navigate(RoomEdit(roomId)) }
        }

        fun navigateToAddPot(roomId: Int) {
            navigationDispatcher.emit { it.navigate(PotEdit(roomId = roomId)) }
        }

        fun navigateToEditPot(
            roomId: Int,
            potId: Int,
        ) {
            navigationDispatcher.emit { it.navigate(PotEdit(roomId = roomId, potId = potId)) }
        }

        @AssistedFactory
        interface Factory : ViewModelAssistedFactory<SoilMoistureListViewModel> {
            override fun create(handle: SavedStateHandle): SoilMoistureListViewModel
        }
    }
