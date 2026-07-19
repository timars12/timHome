package com.timhome.soilmoisture.ui.navigation

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.timhome.base.DaggerBaseComponent
import com.timhome.core.ModularizationApplication
import com.timhome.core.common.navigation.PotEdit
import com.timhome.core.common.navigation.RoomEdit
import com.timhome.core.common.navigation.SoilMoisture
import com.timhome.core.ui.di.ViewModelFactoryContainer
import com.timhome.soilmoisture.di.DaggerSoilMoistureComponent
import com.timhome.soilmoisture.ui.list.SoilMoistureListScreen
import com.timhome.soilmoisture.ui.potedit.PotEditScreen
import com.timhome.soilmoisture.ui.roomedit.RoomEditScreen

fun NavGraphBuilder.soilMoistureRoute() {
    composable<SoilMoisture> {
        val context = LocalContext.current
        val viewModelFactory =
            remember {
                ViewModelFactoryContainer().also {
                    DaggerSoilMoistureComponent.factory()
                        .create(
                            DaggerBaseComponent.factory()
                                .create(ModularizationApplication.coreComponent(context)),
                        ).inject(it)
                }.viewModelFactory
            }
        SoilMoistureListScreen(viewModelFactory)
    }
    composable<RoomEdit> {
        val context = LocalContext.current
        val viewModelFactory =
            remember {
                ViewModelFactoryContainer().also {
                    DaggerSoilMoistureComponent.factory()
                        .create(
                            DaggerBaseComponent.factory()
                                .create(ModularizationApplication.coreComponent(context)),
                        ).inject(it)
                }.viewModelFactory
            }
        RoomEditScreen(viewModelFactory)
    }
    composable<PotEdit> {
        val context = LocalContext.current
        val viewModelFactory =
            remember {
                ViewModelFactoryContainer().also {
                    DaggerSoilMoistureComponent.factory()
                        .create(
                            DaggerBaseComponent.factory()
                                .create(ModularizationApplication.coreComponent(context)),
                        ).inject(it)
                }.viewModelFactory
            }
        PotEditScreen(viewModelFactory)
    }
}
