package com.timhome.base

import com.timhome.core.datastore.DataStoreManager
import com.timhome.core.data.repository.ArduinoRepository
import com.timhome.core.data.repository.ArduinoRepositoryImpl
import com.timhome.core.data.repository.SoilMoistureRepository
import com.timhome.core.data.repository.SoilMoistureRepositoryImpl
import com.timhome.core.common.FeatureScope
import com.timhome.mock.repository.MockArduinoRepositoryImpl
import com.timhome.mock.repository.MockSoilMoistureRepositoryImpl
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

    @FeatureScope
    @Provides
    fun provideSoilMoistureRepository(
        dataStore: DataStoreManager,
        mockRepository: MockSoilMoistureRepositoryImpl,
        repository: SoilMoistureRepositoryImpl,
    ): SoilMoistureRepository {
        return when {
            dataStore.isUseMockDate() -> mockRepository
            else -> repository
        }
    }
}
