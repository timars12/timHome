package com.timhome.core.di

import android.content.Context
import com.timhome.core.database.AppDatabase
import com.timhome.core.database.di.DatabaseModule
import com.timhome.core.datastore.DataStoreManager
import com.timhome.core.network.api.ArduinoApi
import com.timhome.core.network.api.SoilMoistureApi
import com.timhome.core.network.di.NetworkModule
import com.timhome.core.common.NavigationDispatcher
import com.timhome.core.common.di.DefaultDispatcher
import com.timhome.core.common.di.IoDispatcher
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@Singleton
@Component(modules = [NetworkModule::class, DatabaseModule::class, AppModule::class, DispatchersModule::class])
interface CoreComponent {
    val retrofit: Retrofit
    val okHttpClient: OkHttpClient
    val appDatabase: AppDatabase
    val dataStore: DataStoreManager
    val arduinoApi: ArduinoApi
    val soilMoistureApi: SoilMoistureApi
    val firebaseAnalytics: FirebaseAnalytics
    val navigationDispatcher: NavigationDispatcher

    @IoDispatcher
    fun ioDispatcher(): CoroutineDispatcher

    @DefaultDispatcher
    fun defaultDispatcher(): CoroutineDispatcher

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance applicationContext: Context,
        ): CoreComponent
    }
}
