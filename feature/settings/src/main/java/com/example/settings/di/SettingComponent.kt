package com.example.settings.di

import com.example.core.di.CoreComponent
import com.example.core.di.ViewModelFactoryContainer
import com.example.core.di.scope.FeatureScope
import com.example.settings.ui.SettingModule
import dagger.Component

@FeatureScope
@Component(dependencies = [CoreComponent::class], modules = [SettingModule::class])
internal interface SettingComponent {
    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): SettingComponent
    }

    fun inject(container: ViewModelFactoryContainer)
}
