package com.timhome.settings.di

import com.timhome.core.di.CoreComponent
import com.timhome.core.di.ViewModelFactoryContainer
import com.timhome.core.common.FeatureScope
import com.timhome.settings.ui.SettingModule
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
