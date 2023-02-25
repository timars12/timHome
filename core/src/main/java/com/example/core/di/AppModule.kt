package com.example.core.di

import android.content.Context
import com.example.core.data.AppDatabase
import com.example.core.utils.NavigationDispatcher
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
}
