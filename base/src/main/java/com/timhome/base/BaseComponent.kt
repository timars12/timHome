package com.timhome.base

import com.timhome.core.database.AppDatabase
import com.timhome.core.datastore.DataStoreManager
import com.timhome.core.data.repository.ArduinoRepository
import com.timhome.core.data.repository.SoilMoistureRepository
import com.timhome.core.di.CoreComponent
import com.timhome.core.common.FeatureScope
import com.timhome.core.common.NavigationDispatcher
import com.timhome.core.common.di.DefaultDispatcher
import com.timhome.core.common.di.IoDispatcher
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Component
import kotlinx.coroutines.CoroutineDispatcher
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
    val soilMoistureRepository: SoilMoistureRepository
    val firebaseAnalytics: FirebaseAnalytics
    val navigationDispatcher: NavigationDispatcher

    @IoDispatcher
    fun ioDispatcher(): CoroutineDispatcher

    @DefaultDispatcher
    fun defaultDispatcher(): CoroutineDispatcher

    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): BaseComponent
    }
}
