package com.example.module1.di

import android.app.Activity
import com.example.core.coreComponent
import com.example.core.di.CoreComponent
import com.example.module1.MainActivity
import dagger.Component

@Module1Scope
@Component(dependencies = [CoreComponent::class], modules = [ModuleNeedRetrofit::class])
internal interface Module1Component {

    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): Module1Component
    }

    fun inject(activity: MainActivity)
}
