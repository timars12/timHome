package com.timhome.home.ui.navigation

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.timhome.base.DaggerBaseComponent
import com.timhome.core.ModularizationApplication
import com.timhome.core.di.ViewModelFactoryContainer
import com.timhome.core.ui.slideIntoContainer
import com.timhome.core.ui.slideOutOfContainer
import com.timhome.home.di.DaggerHomeComponent
import com.timhome.home.ui.HomeScreen

fun NavGraphBuilder.homeRoute() {
    composable(
        route = "homeScreen",
        enterTransition = { slideIntoContainer() },
        exitTransition = { slideOutOfContainer() },
    ) {
        val context = LocalContext.current
        val viewModelFactory =
            remember {
                ViewModelFactoryContainer().also {
                    DaggerHomeComponent.factory()
                        .create(
                            DaggerBaseComponent.factory()
                                .create(ModularizationApplication.coreComponent(context)),
                        ).inject(it)
                }.viewModelFactory
            }
        HomeScreen(viewModelFactory.get())
    }
}
