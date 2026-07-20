package com.timhome.modularizationtest.di

import com.timhome.base.BaseComponent
import com.timhome.base.BaseScope
import com.timhome.modularizationtest.ui.CO2Worker
import com.timhome.modularizationtest.ui.MainActivity
import dagger.Component

@BaseScope
@Component(dependencies = [BaseComponent::class])
internal interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(baseComponent: BaseComponent): AppComponent
    }

    fun inject(activity: MainActivity)

    fun inject(worker: CO2Worker)
}
