package com.example.core.data.api

import com.example.core.data.api.response.ArduinoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface ArduinoApi {

    @GET
    suspend fun getCo2AndTemperature(@Url ipAddress: String?): Response<ArduinoResponse>
}
