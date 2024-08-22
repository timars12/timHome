package com.example.home.ui.navigation

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.base.DaggerBaseComponent
import com.example.core.ModularizationApplication
import com.example.core.di.ViewModelFactoryContainer
import com.example.core.ui.slideIntoContainer
import com.example.core.ui.slideOutOfContainer
import com.example.home.di.DaggerHomeComponent
import com.example.home.ui.HomeScreen

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
