package com.timhome.core.di

import android.content.Context
import com.timhome.core.database.AppDatabase
import com.timhome.core.common.NavigationDispatcher
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
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
