package com.example.core.data.repository

import com.example.core.data.api.response.ArduinoResponse
import com.example.core.data.db.entity.CarbonDioxideEntity
import com.example.core.utils.CallStatus
import kotlinx.coroutines.flow.Flow

interface ArduinoRepository {
    suspend fun getCo2AndTemperature(): CallStatus<ArduinoResponse>
    fun getCO2ValuesFromDB(): Flow<List<CarbonDioxideEntity>>
}
