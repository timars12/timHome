package com.example.home.data.api.response

import androidx.annotation.Keep

@Keep
data class WeatherResponse(val main: MainTemperature)

@Keep
data class MainTemperature(val temp: Double, val pressure: Int)
