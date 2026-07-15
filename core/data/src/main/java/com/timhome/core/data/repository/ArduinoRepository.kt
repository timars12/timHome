package com.timhome.core.data.repository

import com.timhome.core.network.api.response.ArduinoResponse
import com.timhome.core.database.entity.CarbonDioxideEntity
import com.timhome.core.common.CallStatus
import kotlinx.coroutines.flow.Flow

interface ArduinoRepository {
    suspend fun getCo2AndTemperature(): CallStatus<ArduinoResponse>

    fun getCO2ValuesFromDB(): Flow<List<CarbonDioxideEntity>>
}
