package com.example.device.di

import com.example.core.di.CoreComponent
import com.example.core.di.ViewModelFactoryContainer
import com.example.core.di.scope.FeatureScope
import com.example.device.ui.buyModule.BuyModule
import com.example.device.ui.device.DeviceDetailModule
import com.example.device.ui.listDevice.DeviceListModule
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
