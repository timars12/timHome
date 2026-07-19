package com.timhome.soilmoisture.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timhome.core.designsystem.SnackbarMessage
import com.timhome.core.designsystem.theme.HomeTheme
import com.timhome.core.ui.viewmodel.ViewModelFactory
import com.timhome.soilmoisture.R
import com.timhome.soilmoisture.ui.list.composables.RoomCard

@Composable
internal fun SoilMoistureListScreen(
    abstractFactory: dagger.Lazy<ViewModelFactory>,
    viewModel: SoilMoistureListViewModel = viewModel(factory = abstractFactory.get()),
) {
    HomeTheme {
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(state.snackbarMessage) {
            state.snackbarMessage?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.consumeSnackbarMessage()
            }
        }

        Scaffold(
            snackbarHost = { SnackbarMessage(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(onClick = viewModel::navigateToAddRoom) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(R.string.soil_moisture_add_room))
                }
            },
        ) { padding ->
            if (state.rooms.isEmpty() && !state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.soil_moisture_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.soil_moisture_empty_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    items(items = state.rooms, key = RoomUiModel::id) { room ->
                        RoomCard(
                            room = room,
                            onWaterClick = viewModel::waterPot,
                            onEditRoomClick = { viewModel.navigateToEditRoom(room.id) },
                            onEditPotClick = { potId -> viewModel.navigateToEditPot(room.id, potId) },
                            onAddPotClick = { viewModel.navigateToAddPot(room.id) },
                        )
                    }
                }
            }
        }
    }
}
