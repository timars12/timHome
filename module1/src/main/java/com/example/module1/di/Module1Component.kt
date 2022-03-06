package com.example.module1.di

import com.example.core.di.CoreComponent
import com.example.core.di.scope.FeatureScope
import com.example.module1.MainActivity
import dagger.Component

@FeatureScope
@Component(dependencies = [CoreComponent::class], modules = [ModuleNeedRetrofit::class])
internal interface Module1Component {

    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): Module1Component
    }

    fun inject(activity: MainActivity)
}
