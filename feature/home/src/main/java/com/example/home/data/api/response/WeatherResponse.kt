package com.example.home.data.api.response

import androidx.annotation.Keep

@Keep
internal data class WeatherResponse(val main: MainTemperature)

@Keep
internal data class MainTemperature(val temp: Double, val pressure: Int)
