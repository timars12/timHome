package com.timhome.device.ui.navigation

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.timhome.core.ModularizationApplication
import com.timhome.core.common.navigation.BuyModule
import com.timhome.core.common.navigation.DeviceDetail
import com.timhome.core.common.navigation.Devices
import com.timhome.core.ui.di.ViewModelFactoryContainer
import com.timhome.device.di.DaggerDeviceComponent
import com.timhome.device.ui.buyModule.BuyModuleScreen
import com.timhome.device.ui.device.DeviceDetailScreen
import com.timhome.device.ui.listDevice.DeviceListScreen

fun NavGraphBuilder.deviceRoute() {
    composable<Devices>(
        enterTransition = { com.timhome.core.ui.scaleIntoContainer() },
        exitTransition = { com.timhome.core.ui.scaleOutOfContainer() },
    ) {
        val context = LocalContext.current
        val viewModelFactory =
            remember {
                ViewModelFactoryContainer().also {
                    DaggerDeviceComponent.factory()
                        .create(ModularizationApplication.coreComponent(context)).inject(it)
                }.viewModelFactory
            }
        DeviceListScreen(viewModelFactory)
    }
    composable<DeviceDetail> {
        val context = LocalContext.current
        val viewModelFactory =
            remember {
                ViewModelFactoryContainer().also {
                    DaggerDeviceComponent.factory()
                        .create(ModularizationApplication.coreComponent(context)).inject(it)
                }.viewModelFactory
            }
        DeviceDetailScreen(viewModelFactory)
    }
    composable<BuyModule> {
        val context = LocalContext.current
        val viewModelFactory =
            remember {
                ViewModelFactoryContainer().also {
                    DaggerDeviceComponent.factory()
                        .create(ModularizationApplication.coreComponent(context)).inject(it)
                }.viewModelFactory
            }
        BuyModuleScreen(viewModelFactory)
    }
}
