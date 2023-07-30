package com.example.home.data.api.response

data class WeatherResponse(val main: MainTemperature)

data class MainTemperature(val temp: Double, val pressure: Int)
