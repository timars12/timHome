package com.example.mock.repository

import com.example.core.data.AppDatabase
import com.example.core.data.api.response.ArduinoResponse
import com.example.core.data.db.entity.CarbonDioxideEntity
import com.example.core.data.repository.ArduinoRepository
import com.example.core.utils.CallStatus
import com.example.mock.api.Co2AndTemperatureMock
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

open class MockArduinoRepositoryImpl
    @Inject
    constructor(
        private val mockAPI: Co2AndTemperatureMock,
        private val database: AppDatabase,
    ) : ArduinoRepository {
        override suspend fun getCo2AndTemperature(): CallStatus<ArduinoResponse> {
            return CallStatus.Success(mockAPI.getCo2AndTemperature())
        }

        override fun getCO2ValuesFromDB(): Flow<List<CarbonDioxideEntity>> {
            return database.carbonDioxideDao().getAllCO2Levels()
        }
    }
