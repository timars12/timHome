package com.example.home.di

import com.example.base.BaseComponent
import com.example.base.BaseScope
import com.example.home.ui.HomeFragment
import com.example.home.ui.home.HomeModule
import dagger.Component

@BaseScope
@Component(dependencies = [BaseComponent::class], modules = [HomeModule::class])
internal interface HomeComponent {
    @Component.Factory
    interface Factory {
        fun create(baseComponent: BaseComponent): HomeComponent
    }

    fun inject(fragment: HomeFragment)
}
