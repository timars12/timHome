package com.example.home.data.api

import com.example.home.data.api.response.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET

interface WeatherApi {

    //todo lat, lon, appid
    @GET("https://api.openweathermap.org/data/2.5/weather?lat=48.9226&lon=24.7111&units=metric&appid=deff678822cba701b1e6f9048e5fea4c")
    suspend fun getWeatherInLocation(): Response<WeatherResponse>
}