package com.timhome.device.di

import com.timhome.core.di.CoreComponent
import com.timhome.core.ui.di.ViewModelFactoryContainer
import com.timhome.core.common.FeatureScope
import com.timhome.device.ui.buyModule.BuyModule
import com.timhome.device.ui.device.DeviceDetailModule
import com.timhome.device.ui.listDevice.DeviceListModule
import dagger.Component

@FeatureScope
@Component(
    dependencies = [CoreComponent::class],
    modules = [DeviceListModule::class, DeviceDetailModule::class, BuyModule::class],
)
internal interface DeviceComponent {
    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): DeviceComponent
    }

    fun inject(container: ViewModelFactoryContainer)
}
