package com.timhome.core.database.di

import android.content.Context
import com.timhome.core.database.AppDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(context: Context): AppDatabase =
        AppDatabase.getInstance(context)
}
