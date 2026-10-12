package com.timhome.home.ui.navigation

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.EntryProviderScope
import com.timhome.base.DaggerBaseComponent
import com.timhome.core.ModularizationApplication
import com.timhome.core.common.navigation.Home
import com.timhome.core.common.navigation.Route
import com.timhome.core.ui.di.ViewModelFactoryContainer
import com.timhome.core.ui.slideTransition
import com.timhome.home.di.DaggerHomeComponent
import com.timhome.home.ui.HomeScreen

fun EntryProviderScope<Route>.homeEntries() {
    entry<Home>(metadata = slideTransition()) {
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
