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
)
