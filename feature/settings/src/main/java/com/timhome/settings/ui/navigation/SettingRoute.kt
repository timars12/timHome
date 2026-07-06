package com.timhome.settings.ui.navigation

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.timhome.core.ModularizationApplication
import com.timhome.core.di.ViewModelFactoryContainer
import com.timhome.settings.di.DaggerSettingComponent
import com.timhome.settings.ui.SettingScreen

fun NavGraphBuilder.settingRoute() {
    composable(
        route = "settingScreen",
        enterTransition = { com.timhome.core.ui.scaleIntoContainer() },
        exitTransition = { com.timhome.core.ui.scaleOutOfContainer() },
    ) {
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
