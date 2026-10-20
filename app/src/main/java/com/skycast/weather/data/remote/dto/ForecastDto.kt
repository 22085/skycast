package com.skycast.weather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    @SerialName("utc_offset_seconds") val utcOffsetSeconds: Int,
    val elevation: Double? = null,
    val current: CurrentWeather? = null,
    val hourly: HourlyForecast? = null,
    val daily: DailyForecast? = null,
)

@Serializable
data class CurrentWeather(
    val time: String,
    val interval: Int? = null,
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("relative_humidity_2m") val humidity: Int,
    @SerialName("apparent_temperature") val apparentTemperature: Double,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("wind_speed_10m") val windSpeed: Double,
    @SerialName("wind_direction_10m") val windDirection: Int,
    @SerialName("surface_pressure") val pressure: Double? = null,
    @SerialName("is_day") val isDay: Int? = null,
    @SerialName("cloud_cover") val cloudCover: Int? = null,
)

@Serializable
data class HourlyForecast(
    val time: List<String>,
    @SerialName("temperature_2m") val temperature: List<Double>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("precipitation_probability") val precipitationProbability: List<Int>? = null,
    @SerialName("is_day") val isDay: List<Int>? = null,
)

@Serializable
data class DailyForecast(
    val time: List<String>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("temperature_2m_max") val temperatureMax: List<Double>,
    @SerialName("temperature_2m_min") val temperatureMin: List<Double>,
    val sunrise: List<String>? = null,
    val sunset: List<String>? = null,
    @SerialName("precipitation_probability_max") val precipitationProbabilityMax: List<Int>? = null,
    @SerialName("wind_speed_10m_max") val windSpeedMax: List<Double>? = null,
    @SerialName("uv_index_max") val uvIndexMax: List<Double>? = null,
)
