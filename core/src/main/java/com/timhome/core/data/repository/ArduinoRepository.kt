package com.timhome.core.data.repository

import com.timhome.core.data.api.response.ArduinoResponse
import com.timhome.core.data.db.entity.CarbonDioxideEntity
import com.timhome.core.utils.CallStatus
import kotlinx.coroutines.flow.Flow

interface ArduinoRepository {
    suspend fun getCo2AndTemperature(): CallStatus<ArduinoResponse>

    fun getCO2ValuesFromDB(): Flow<List<CarbonDioxideEntity>>
}
