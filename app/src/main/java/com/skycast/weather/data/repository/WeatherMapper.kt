package com.skycast.weather.data.repository

import com.skycast.weather.data.remote.dto.AirQualityResponse
import com.skycast.weather.data.remote.dto.CurrentWeather as CurrentDto
import com.skycast.weather.data.remote.dto.DailyForecast
import com.skycast.weather.data.remote.dto.HourlyForecast
import com.skycast.weather.domain.model.AirQuality
import com.skycast.weather.domain.model.CurrentWeather
import com.skycast.weather.domain.model.DailyWeather
import com.skycast.weather.domain.model.HourlyWeather
import com.skycast.weather.domain.model.toWeatherCondition
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * DTO → 领域模型映射。
 */
internal object WeatherMapper {

    private val hourFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val dayFormatter = DateTimeFormatter.ofPattern("M月d日 E", Locale.getDefault())

    fun mapCurrent(dto: CurrentDto): CurrentWeather = CurrentWeather(
        temperatureC = dto.temperature.toInt(),
        feelsLikeC = dto.apparentTemperature.toInt(),
        condition = dto.weatherCode.toWeatherCondition(),
        isDay = (dto.isDay ?: 1) == 1,
        humidityPercent = dto.humidity,
        windSpeedKmh = dto.windSpeed,
        windDirectionDeg = dto.windDirection,
        pressureHpa = dto.pressure,
        cloudCoverPercent = dto.cloudCover,
        observedAt = dto.time,
    )

    fun mapHourly(dto: HourlyForecast, zone: ZoneId, limit: Int): List<HourlyWeather> {
        val count = minOf(dto.time.size, limit)
        return (0 until count).map { i ->
            val time = dto.time[i]
            HourlyWeather(
                timeIso = time,
                hourLabel = time.hourLabel(zone),
                temperatureC = dto.temperature.getOrElse(i) { 0.0 }.toInt(),
                condition = dto.weatherCode.getOrElse(i) { -1 }.toWeatherCondition(),
                isDay = dto.isDay?.getOrNull(i)?.let { it == 1 } ?: true,
                precipitationProbability = dto.precipitationProbability?.getOrNull(i),
            )
        }
    }

    fun mapDaily(dto: DailyForecast, zone: ZoneId): List<DailyWeather> {
        val count = dto.time.size
        return (0 until count).map { i ->
            val date = dto.time[i]
            DailyWeather(
                dateIso = date,
                dayLabel = date.dayLabel(zone),
                condition = dto.weatherCode.getOrElse(i) { -1 }.toWeatherCondition(),
                maxTempC = dto.temperatureMax.getOrElse(i) { 0.0 }.toInt(),
                minTempC = dto.temperatureMin.getOrElse(i) { 0.0 }.toInt(),
                sunriseIso = dto.sunrise?.getOrNull(i),
                sunsetIso = dto.sunset?.getOrNull(i),
                precipitationProbability = dto.precipitationProbabilityMax?.getOrNull(i),
                windSpeedMaxKmh = dto.windSpeedMax?.getOrNull(i),
                uvIndexMax = dto.uvIndexMax?.getOrNull(i),
            )
        }
    }

    fun mapAirQuality(dto: AirQualityResponse): AirQuality? {
        val current = dto.current ?: return null
        return AirQuality(
            europeanAqi = current.europeanAqi?.toInt(),
            usAqi = current.usAqi?.toInt(),
            pm25 = current.pm25,
            pm10 = current.pm10,
            ozone = current.ozone,
        )
    }

    private fun String.hourLabel(zone: ZoneId): String = try {
        val instant = Instant.parse(this)
        hourFormatter.format(instant.atZone(zone))
    } catch (_: Exception) {
        takeLast(5)
    }

    private fun String.dayLabel(zone: ZoneId): String = try {
        val instant = Instant.parse(this)
        dayFormatter.format(instant.atZone(zone))
    } catch (_: Exception) {
        this
    }
}


