package com.timhome.base

import com.timhome.core.data.AppDatabase
import com.timhome.core.data.DataStoreManager
import com.timhome.core.data.repository.ArduinoRepository
import com.timhome.core.di.CoreComponent
import com.timhome.core.common.FeatureScope
import com.timhome.core.common.NavigationDispatcher
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Component
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@FeatureScope
@Component(dependencies = [CoreComponent::class], modules = [BaseModule::class])
interface BaseComponent {
    val retrofit: Retrofit
    val okHttpClient: OkHttpClient
    val appDatabase: AppDatabase
    val dataStore: DataStoreManager
    val arduinoRepository: ArduinoRepository
    val firebaseAnalytics: FirebaseAnalytics
    val navigationDispatcher: NavigationDispatcher

    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): BaseComponent
    }
}
