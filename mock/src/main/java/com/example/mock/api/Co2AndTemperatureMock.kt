package com.example.mock.api

import com.example.core.data.api.response.ArduinoResponse
import javax.inject.Inject


class Co2AndTemperatureMock @Inject constructor() {

    @Volatile
    private var count: Int = 0
        set(value) {
            field = if (value >= 5000) 468 else value
        }

    fun getCo2AndTemperature(): ArduinoResponse {
        count++
        return ArduinoResponse(temperature = 21.8, co2 = 468 + count)
    }
}
