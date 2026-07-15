package com.timhome.base

import com.timhome.core.datastore.DataStoreManager
import com.timhome.core.data.repository.ArduinoRepository
import com.timhome.core.data.repository.ArduinoRepositoryImpl
import com.timhome.core.common.FeatureScope
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
