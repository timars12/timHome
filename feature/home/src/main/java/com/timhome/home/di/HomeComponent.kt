package com.timhome.home.di

import com.timhome.base.BaseComponent
import com.timhome.base.BaseScope
import com.timhome.core.di.ViewModelFactoryContainer
import dagger.Component

@BaseScope
@Component(dependencies = [BaseComponent::class], modules = [HomeModule::class])
internal interface HomeComponent {
    @Component.Factory
    interface Factory {
        fun create(baseComponent: BaseComponent): HomeComponent
    }

    fun inject(container: ViewModelFactoryContainer)
}
