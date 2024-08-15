package com.example.device.ui.navigation

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.core.ModularizationApplication
import com.example.core.di.ViewModelFactoryContainer
import com.example.device.di.DaggerDeviceComponent
import com.example.device.ui.buyModule.BuyModuleScreen
import com.example.device.ui.device.DeviceDetailScreen
import com.example.device.ui.listDevice.DeviceListScreen

fun NavGraphBuilder.deviceRoute() {
    composable(
        route = "devicesScreen",
        enterTransition = { com.example.core.ui.scaleIntoContainer() },
        exitTransition = { com.example.core.ui.scaleOutOfContainer() },
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
    composable(
        route = "deviceDetailScreen/{deviceId}",
        arguments = listOf(navArgument("deviceId") { type = NavType.IntType }),
    ) {
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
    composable(
        route = "buyModuleScreen/{deviceId}",
        arguments = listOf(navArgument("deviceId") { type = NavType.IntType }),
    ) {
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
