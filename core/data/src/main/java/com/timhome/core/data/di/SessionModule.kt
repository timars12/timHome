package com.timhome.core.data.di

import com.timhome.core.data.repository.DataStoreSessionRepository
import com.timhome.core.data.repository.SessionRepository
import com.timhome.core.datastore.DataStoreManager
import dagger.Module
import dagger.Provides

// @Provides, not @Binds: a public @Binds function would expose the internal implementation.
@Module
object SessionModule {
    @Provides
    fun provideSessionRepository(dataStore: DataStoreManager): SessionRepository = DataStoreSessionRepository(dataStore)
}
