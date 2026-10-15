package com.timhome.settings.ui.navigation

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.EntryProviderScope
import com.timhome.core.ModularizationApplication
import com.timhome.core.common.navigation.Route
import com.timhome.core.common.navigation.Setting
import com.timhome.core.ui.di.ViewModelFactoryContainer
import com.timhome.core.ui.scaleTransition
import com.timhome.settings.di.DaggerSettingComponent
import com.timhome.settings.ui.SettingScreen

fun EntryProviderScope<Route>.settingEntries() {
    entry<Setting>(metadata = scaleTransition()) {
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
