package com.example.home.di

import com.example.core.di.CoreComponent
import com.example.core.di.scope.FeatureScope
import com.example.home.presentation.HomeFragment
import dagger.Component

@FeatureScope
@Component(dependencies = [CoreComponent::class])
internal interface HomeComponent {
    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): HomeComponent
    }

    fun inject(fragment: HomeFragment)
}