package com.timhome.soilmoisture.di

import com.timhome.base.BaseComponent
import com.timhome.base.BaseScope
import com.timhome.core.ui.di.ViewModelFactoryContainer
import com.timhome.soilmoisture.ui.list.SoilMoistureListModule
import com.timhome.soilmoisture.ui.potedit.PotEditModule
import com.timhome.soilmoisture.ui.roomedit.RoomEditModule
import dagger.Component

@BaseScope
@Component(
    dependencies = [BaseComponent::class],
    modules = [SoilMoistureListModule::class, RoomEditModule::class, PotEditModule::class],
)
internal interface SoilMoistureComponent {
    @Component.Factory
    interface Factory {
        fun create(baseComponent: BaseComponent): SoilMoistureComponent
    }

    fun inject(container: ViewModelFactoryContainer)
}
