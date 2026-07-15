package com.timhome.core.di

import android.content.Context
import com.timhome.core.data.AppDatabase
import com.timhome.core.data.DataStoreManager
import com.timhome.core.data.api.ArduinoApi
import com.timhome.core.common.NavigationDispatcher
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@Singleton
@Component(modules = [NetworkModule::class, AppModule::class])
interface CoreComponent {
    val retrofit: Retrofit
    val okHttpClient: OkHttpClient
    val appDatabase: AppDatabase
    val dataStore: DataStoreManager
    val arduinoApi: ArduinoApi
    val firebaseAnalytics: FirebaseAnalytics
    val navigationDispatcher: NavigationDispatcher

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance applicationContext: Context,
        ): CoreComponent
    }
}
