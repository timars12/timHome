package com.example.authdynamic.di

import com.example.authdynamic.presentation.signin.SignInFragment
import com.example.authdynamic.presentation.signin.di.SignInModule
import com.example.core.di.CoreComponent
import com.example.core.di.scope.FeatureScope
import dagger.Component

@FeatureScope
@Component(
    dependencies = [CoreComponent::class],
    modules = [LoginModule::class, SignInModule::class]
)
internal interface AuthenticationComponent {
    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): AuthenticationComponent
    }

    fun inject(fragment: SignInFragment)
}
