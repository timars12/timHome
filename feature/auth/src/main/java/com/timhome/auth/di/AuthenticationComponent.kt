package com.timhome.auth.di

import com.timhome.auth.ui.signin.di.SignInModule
import com.timhome.core.di.CoreComponent
import com.timhome.core.di.ViewModelFactoryContainer
import com.timhome.core.common.FeatureScope
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
