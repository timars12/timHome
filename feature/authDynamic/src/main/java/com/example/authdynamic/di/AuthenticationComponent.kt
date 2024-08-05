package com.example.authdynamic.di

import com.example.authdynamic.ui.signin.di.SignInModule
import com.example.core.di.CoreComponent
import com.example.core.di.ViewModelFactoryContainer
import com.example.core.di.scope.FeatureScope
import dagger.Component

@FeatureScope
@Component(
    dependencies = [CoreComponent::class],
    modules = [LoginModule::class, SignInModule::class],
)
internal interface AuthenticationComponent {
    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): AuthenticationComponent
    }

    fun inject(container: ViewModelFactoryContainer)
}
