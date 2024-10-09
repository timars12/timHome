package com.example.device.ui.listDevice

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
import com.example.core.ui.theme.HomeTheme
import com.example.core.utils.trace
import com.example.core.utils.viewmodel.ViewModelFactory
import com.example.device.domain.models.DeviceModel
import com.example.device.ui.listDevice.composables.DeviceListItem

const val SELECTED_DEVICE_ID = "deviceId"

@Composable
internal fun DeviceListScreen(
    abstractFactory: dagger.Lazy<ViewModelFactory>,
    viewModel: DeviceListViewModel = viewModel(factory = abstractFactory.get()),
) {
    HomeTheme {
        val deviceList by viewModel.deviceList.collectAsStateWithLifecycle()

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
