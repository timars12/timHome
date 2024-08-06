package com.example.settings.ui.navigation

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.core.ModularizationApplication
import com.example.core.di.ViewModelFactoryContainer
import com.example.settings.di.DaggerSettingComponent
import com.example.settings.ui.SettingScreen

fun NavGraphBuilder.settingRoute() {
    composable(route = "settingScreen") {
        val context = LocalContext.current
        val viewModelFactory =
            remember {
                ViewModelFactoryContainer().also {
                    DaggerSettingComponent.factory()
                        .create(ModularizationApplication.coreComponent(context)).inject(it)
                }.viewModelFactory
            }
        SettingScreen(viewModelFactory)
    }
}
