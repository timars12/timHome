package com.example.mock.api

import com.example.core.data.api.response.ArduinoResponse
import javax.inject.Inject
import kotlin.random.Random


class Co2AndTemperatureMock @Inject constructor() {

    fun getCo2AndTemperature(): ArduinoResponse {
        return ArduinoResponse(
            temperature = Random.nextDouble(18.0, 24.0),
            co2 = Random.nextInt(350, 5000)
        )
    }
}
