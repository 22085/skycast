package com.skycast.weather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AirQualityResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentAirQuality? = null,
)

@Serializable
data class CurrentAirQuality(
    val time: String,
    @SerialName("european_aqi") val europeanAqi: Double? = null,
    @SerialName("us_aqi") val usAqi: Double? = null,
    @SerialName("pm2_5") val pm25: Double? = null,
    @SerialName("pm10") val pm10: Double? = null,
    @SerialName("ozone") val ozone: Double? = null,
)
