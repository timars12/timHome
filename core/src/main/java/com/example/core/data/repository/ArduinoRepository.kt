package com.example.core.data.repository

import com.example.core.data.AppDatabase
import com.example.core.data.DataStoreManager
import com.example.core.data.api.ArduinoApi
import com.example.core.data.api.response.ArduinoResponse
import com.example.core.data.db.entity.CarbonDioxideEntity
import com.example.core.utils.CallStatus
import com.example.core.utils.Constant.CODE_200
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ArduinoRepository @Inject constructor(
    private val dataStore: DataStoreManager,
    private val database: AppDatabase,
    private val arduinoApi: ArduinoApi
) {

    suspend fun getCo2AndTemperature(): CallStatus<ArduinoResponse> {
        return try {
            val response = arduinoApi.getCo2AndTemperature("http://${dataStore.getHomeIpAddress()}/")
            when {
                response.code() == CODE_200 -> CallStatus.Success(response.body())
                else -> CallStatus.Error()
            }
        } catch (exception: Exception) {
            CallStatus.Error()
        }
    }

    fun getCO2ValuesFromDB(): Flow<List<CarbonDioxideEntity>> {
        return database.carbonDioxideDao().getAllCO2Levels()
    }
}
