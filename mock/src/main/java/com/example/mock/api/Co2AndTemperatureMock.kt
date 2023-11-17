package com.example.mock.api

import com.example.core.data.api.response.ArduinoResponse
import javax.inject.Inject
import kotlin.random.Random

private const val TEMPERATURE_FROM = 18.0
private const val TEMPERATURE_TO = 24.0
private const val CO2_FROM = 350
private const val CO2_TO = 5000

class Co2AndTemperatureMock @Inject constructor() {

    fun getCo2AndTemperature(): ArduinoResponse {
        return ArduinoResponse(
            temperature = Random.nextDouble(TEMPERATURE_FROM, TEMPERATURE_TO),
            co2 = Random.nextInt(CO2_FROM, CO2_TO)
        )
    }
}
