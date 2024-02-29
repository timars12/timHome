package com.example.core.di

import android.content.Context
import com.example.core.data.AppDatabase
import com.example.core.utils.NavigationDispatcher
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {
    @Singleton
    @Provides
    fun provideAppDatabase(context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Singleton
    @Provides
    fun provideNavDispatcher(): NavigationDispatcher = NavigationDispatcher()

    @Singleton
    @Provides
    fun provideFirebaseAnalytics(): FirebaseAnalytics = Firebase.analytics
}
