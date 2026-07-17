package com.timhome.device.ui.listDevice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timhome.core.designsystem.theme.HomeTheme
import com.timhome.core.common.trace
import com.timhome.core.ui.viewmodel.ViewModelFactory
import com.timhome.device.domain.models.DeviceModel
import com.timhome.device.ui.listDevice.composables.DeviceListItem

const val SELECTED_DEVICE_ID = "deviceId"

@Composable
internal fun DeviceListScreen(
    abstractFactory: dagger.Lazy<ViewModelFactory>,
    viewModel: DeviceListViewModel = viewModel(factory = abstractFactory.get()),
) {
    HomeTheme {
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val deviceList = state.devices

        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("device_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            trace("deviceList") {
                items(items = deviceList, key = DeviceModel::id) { item ->
                    DeviceListItem(
                        item = item,
                        navigateToDetailScreen = {
                            viewModel.navigateToDetailScreen(
                                item,
                            )
                        },
                    )
                }
            }
        }
    }
}
