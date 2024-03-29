package com.example.home.data.repository

import com.example.core.data.DataStoreManager
import com.example.core.utils.CallStatus
import com.example.core.utils.Constant.CODE_200
import com.example.home.data.api.WeatherApi
import com.example.home.data.api.response.WeatherResponse
import javax.inject.Inject

internal class WeatherRepository
    @Inject
    constructor(
        private val dataStore: DataStoreManager,
        private val weatherApi: WeatherApi,
    ) {
        suspend fun getWeather(): CallStatus<WeatherResponse> {
            return try {
                val response = weatherApi.getWeatherInLocation()
                when {
                    response.code() == CODE_200 -> CallStatus.Success(response.body())
                    else -> CallStatus.Error()
                }
            } catch (e: Exception) {
                CallStatus.Error()
            }
        }
    }
