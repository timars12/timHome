package com.example.modularizationtest.di

import com.example.core.di.CoreComponent
import com.example.core.di.scope.FeatureScope
import com.example.modularizationtest.presentation.MainActivity
import dagger.Component

@FeatureScope
@Component(dependencies = [CoreComponent::class])
internal interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): AppComponent
    }

    fun inject(activity: MainActivity)
}
