package com.example.core.data.repository

import com.example.core.data.DataStoreManager
import com.example.core.data.api.ArduinoApi
import com.example.core.data.api.response.ArduinoResponse
import com.example.core.utils.CallStatus
import javax.inject.Inject

class ArduinoRepository @Inject constructor(
    private val dataStore: DataStoreManager,
    private val arduinoApi: ArduinoApi
) {

    suspend fun getCo2AndTemperature(): CallStatus<ArduinoResponse> {
        return try {
            val response = arduinoApi.getCo2AndTemperature("http://${dataStore.getHomeIpAddress()}/")
            when{
                response.code() == 200 -> CallStatus.Success(response.body())
                else -> CallStatus.Error()
            }
        } catch (e: Exception) {
            CallStatus.Error()
        }
    }
}