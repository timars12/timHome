package com.example.authdynamic.di

import com.example.core.di.CoreComponent
import com.example.core.di.scope.FeatureScope
import dagger.Component

@FeatureScope
@Component(dependencies = [CoreComponent::class], modules = [LoginModule::class])
internal interface AuthenticationComponent {
    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): AuthenticationComponent
    }
}
