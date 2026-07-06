package com.timhome.base

import com.timhome.core.data.DataStoreManager
import com.timhome.core.data.repository.ArduinoRepository
import com.timhome.core.data.repository.ArduinoRepositoryImpl
import com.timhome.core.di.scope.FeatureScope
import com.timhome.mock.repository.MockArduinoRepositoryImpl
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
