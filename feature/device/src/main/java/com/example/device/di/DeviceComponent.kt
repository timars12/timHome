package com.example.device.di

import android.content.Context
import com.example.core.ModularizationApplication
import com.example.core.di.CoreComponent
import com.example.core.di.scope.FeatureScope
import com.example.core.utils.viewmodel.ViewModelFactory
import com.example.device.ui.buyModule.BuyModule
import com.example.device.ui.device.DeviceDetailModule
import com.example.device.ui.listDevice.DeviceListModule
import dagger.Component
import javax.inject.Inject

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

    fun inject(fragment: InjectDaggerDependencyImpl)
}

interface InjectDaggerDependency {
    fun inject(context: Context)

    fun getAbstractFactory(): ViewModelFactory
}

internal class InjectDaggerDependencyImpl : InjectDaggerDependency {
    @Inject
    lateinit var abstractFactory: dagger.Lazy<ViewModelFactory>

    override fun inject(context: Context) {
        DaggerDeviceComponent.factory()
            .create(ModularizationApplication.coreComponent(context)).inject(this)
    }

    override fun getAbstractFactory(): ViewModelFactory = abstractFactory.get()
}
