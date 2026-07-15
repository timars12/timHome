package com.timhome.home.data.repository

import com.timhome.core.datastore.DataStoreManager
import com.timhome.core.common.CallStatus
import com.timhome.core.common.Constant.CODE_200
import com.timhome.home.data.api.WeatherApi
import com.timhome.home.data.api.response.WeatherResponse
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
