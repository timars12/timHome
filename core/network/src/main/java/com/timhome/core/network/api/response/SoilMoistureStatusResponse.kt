package com.timhome.core.network.api.response

data class SoilMoistureStatusResponse(
    val bme: Boolean,
    val temp: Double,
    val hum: Double,
    val pres: Double,
    val pots: List<PotReading>,
)

data class PotReading(
    val id: Int,
    val name: String,
    val valid: Boolean,
    val pct: Int,
    val threshold: Int,
    val pump: Boolean,
    // The ESP32 latches this when it watered but soil moisture did not rise
    // (empty tank / blocked line). The app only mirrors and notifies on it.
    val waterAlarm: Boolean = false,
)
