package com.timhome.device.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.EntryProviderScope
import com.timhome.core.ModularizationApplication
import com.timhome.core.common.navigation.BuyModule
import com.timhome.core.common.navigation.DeviceDetail
import com.timhome.core.common.navigation.Devices
import com.timhome.core.common.navigation.Route
import com.timhome.core.ui.di.ViewModelFactoryContainer
import com.timhome.core.ui.scaleTransition
import com.timhome.core.ui.viewmodel.ViewModelFactory
import com.timhome.device.di.DaggerDeviceComponent
import com.timhome.device.ui.buyModule.BuyModuleScreen
import com.timhome.device.ui.device.DeviceDetailScreen
import com.timhome.device.ui.listDevice.DeviceListScreen

fun EntryProviderScope<Route>.deviceEntries() {
    entry<Devices>(metadata = scaleTransition()) {
        DeviceListScreen(rememberDeviceViewModelFactory())
    }
    entry<DeviceDetail>(metadata = scaleTransition()) { key ->
        DeviceDetailScreen(rememberDeviceViewModelFactory(), route = key)
    }
    entry<BuyModule>(metadata = scaleTransition()) { key ->
        BuyModuleScreen(rememberDeviceViewModelFactory(), route = key)
    }
}

@Composable
private fun rememberDeviceViewModelFactory(): dagger.Lazy<ViewModelFactory> {
    val context = LocalContext.current
    return remember {
        ViewModelFactoryContainer().also {
            DaggerDeviceComponent.factory()
                .create(ModularizationApplication.coreComponent(context)).inject(it)
        }.viewModelFactory
    }
}
