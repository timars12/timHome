package com.example.device.di

import com.example.core.di.CoreComponent
import com.example.core.di.scope.FeatureScope
import com.example.device.presentation.device.DeviceDetailFragment
import com.example.device.presentation.device.DeviceDetailModel
import com.example.device.presentation.listDevice.DeviceListFragment
import com.example.device.presentation.listDevice.DeviceListModule
import dagger.Component

@FeatureScope
@Component(
    dependencies = [CoreComponent::class],
    modules = [DeviceListModule::class, DeviceDetailModel::class]
)
internal interface DeviceComponent {
    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): DeviceComponent
    }

    fun inject(fragment: DeviceListFragment)
    fun inject(fragment: DeviceDetailFragment)
}
