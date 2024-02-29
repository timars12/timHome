package com.example.base

import com.example.core.data.DataStoreManager
import com.example.core.data.repository.ArduinoRepository
import com.example.core.data.repository.ArduinoRepositoryImpl
import com.example.core.di.scope.FeatureScope
import com.example.mock.repository.MockArduinoRepositoryImpl
import dagger.Module
import dagger.Provides

@Module
class BaseModule {
    @FeatureScope
    @Provides
    fun provideArduinoRepository(
        dataStore: DataStoreManager,
        mockRepository: MockArduinoRepositoryImpl,
        repository: ArduinoRepositoryImpl,
    ): ArduinoRepository {
        return when {
            dataStore.isUseMockDate() -> mockRepository
            else -> repository
        }
    }
}
