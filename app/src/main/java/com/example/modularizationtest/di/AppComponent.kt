package com.example.modularizationtest.di

import com.example.base.BaseComponent
import com.example.base.BaseScope
import com.example.modularizationtest.presentation.CO2JobService
import com.example.modularizationtest.presentation.MainActivity
import dagger.Component

@BaseScope
@Component(dependencies = [BaseComponent::class])
internal interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(coreComponent: BaseComponent): AppComponent
    }

    fun inject(activity: MainActivity)
    fun inject(service: CO2JobService)
}
