package com.example.home.di

import com.example.base.BaseComponent
import com.example.base.BaseScope
import com.example.core.di.ViewModelFactoryContainer
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
