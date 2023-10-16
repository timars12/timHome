package com.example.base

import com.example.core.data.AppDatabase
import com.example.core.data.DataStoreManager
import com.example.core.data.repository.ArduinoRepository
import com.example.core.di.CoreComponent
import com.example.core.di.scope.FeatureScope
import com.example.core.utils.NavigationDispatcher
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
