package com.skycast.weather.domain.model

import androidx.annotation.DrawableRes
import com.skycast.weather.R

/** WMO 4677 / Open-Meteo 天气代码分类。 */
enum class WeatherCondition {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    FREEZING_RAIN,
    SNOW,
    SNOW_GRAINS,
    RAIN_SHOWERS,
    SNOW_SHOWERS,
    THUNDERSTORM,
    THUNDERSTORM_HAIL,
    UNKNOWN,
}

/**
 * 天气呈现数据：图标、文案、是否适合作为深色背景。
 */
data class WeatherVisual(
    val condition: WeatherCondition,
    val isDay: Boolean,
) {
    @get:DrawableRes
    val iconRes: Int
        get() = when (condition) {
            WeatherCondition.CLEAR ->
                if (isDay) R.drawable.ic_weather_sunny else R.drawable.ic_weather_clear_night
            WeatherCondition.PARTLY_CLOUDY ->
                if (isDay) R.drawable.ic_weather_partly_cloudy_day
                else R.drawable.ic_weather_partly_cloudy_night
            WeatherCondition.CLOUDY -> R.drawable.ic_weather_cloudy
            WeatherCondition.FOG -> R.drawable.ic_weather_fog
            WeatherCondition.DRIZZLE -> R.drawable.ic_weather_drizzle
            WeatherCondition.RAIN, WeatherCondition.FREEZING_RAIN -> R.drawable.ic_weather_rain
            WeatherCondition.SNOW -> R.drawable.ic_weather_snow
            WeatherCondition.SNOW_GRAINS -> R.drawable.ic_weather_snow
            WeatherCondition.RAIN_SHOWERS -> R.drawable.ic_weather_showers
            WeatherCondition.SNOW_SHOWERS -> R.drawable.ic_weather_snow
            WeatherCondition.THUNDERSTORM, WeatherCondition.THUNDERSTORM_HAIL ->
                R.drawable.ic_weather_thunderstorm
            WeatherCondition.UNKNOWN -> R.drawable.ic_weather_cloudy
        }

    val descriptionRes: Int
        get() = when (condition) {
            WeatherCondition.CLEAR -> R.string.cond_clear
            WeatherCondition.PARTLY_CLOUDY -> R.string.cond_partly_cloudy
            WeatherCondition.CLOUDY -> R.string.cond_cloudy
            WeatherCondition.FOG -> R.string.cond_fog
            WeatherCondition.DRIZZLE -> R.string.cond_drizzle
            WeatherCondition.RAIN -> R.string.cond_rain
            WeatherCondition.FREEZING_RAIN -> R.string.cond_freezing_rain
            WeatherCondition.SNOW -> R.string.cond_snow
            WeatherCondition.SNOW_GRAINS -> R.string.cond_snow_grains
            WeatherCondition.RAIN_SHOWERS -> R.string.cond_showers
            WeatherCondition.SNOW_SHOWERS -> R.string.cond_snow_showers
            WeatherCondition.THUNDERSTORM -> R.string.cond_thunderstorm
            WeatherCondition.THUNDERSTORM_HAIL -> R.string.cond_thunderstorm_hail
            WeatherCondition.UNKNOWN -> R.string.cond_unknown
        }
}

/**
 * 当前天气。
 */
data class CurrentWeather(
    val temperatureC: Int,
    val feelsLikeC: Int,
    val condition: WeatherCondition,
    val isDay: Boolean,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val windDirectionDeg: Int,
    val pressureHpa: Double?,
    val cloudCoverPercent: Int?,
    val observedAt: String,
) {
    val visual: WeatherVisual get() = WeatherVisual(condition, isDay)
}

/**
 * 逐小时预报。
 */
data class HourlyWeather(
    val timeIso: String,
    val hourLabel: String,
    val temperatureC: Int,
    val condition: WeatherCondition,
    val isDay: Boolean,
    val precipitationProbability: Int?,
) {
    val visual: WeatherVisual get() = WeatherVisual(condition, isDay)
}

/**
 * 单日预报。
 */
data class DailyWeather(
    val dateIso: String,
    val dayLabel: String,
    val condition: WeatherCondition,
    val maxTempC: Int,
    val minTempC: Int,
    val sunriseIso: String?,
    val sunsetIso: String?,
    val precipitationProbability: Int?,
    val windSpeedMaxKmh: Double?,
    val uvIndexMax: Double?,
) {
    val visual: WeatherVisual get() = WeatherVisual(condition, true)
}

/**
 * 空气质量。
 */
data class AirQuality(
    val europeanAqi: Int?,
    val usAqi: Int?,
    val pm25: Double?,
    val pm10: Double?,
    val ozone: Double?,
) {
    /** 取两者中更保守的一个作为主展示值。 */
    val displayAqi: Int? get() = europeanAqi ?: usAqi

    val levelRes: Int
        get() = when (displayAqi ?: -1) {
            in 0..20 -> R.string.aqi_good
            in 21..40 -> R.string.aqi_fair
            in 41..60 -> R.string.aqi_moderate
            in 61..80 -> R.string.aqi_poor
            in 81..100 -> R.string.aqi_very_poor
            else -> R.string.aqi_extremely_poor
        }
}

/**
 * 一个地点的完整天气。
 */
data class Weather(
    val locationName: String,
    val regionName: String?,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentWeather,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeather>,
    val airQuality: AirQuality?,
    val isCurrentLocation: Boolean,
)

/**
 * WMO weather_code 到枚举的映射。
 * 参考 https://open-meteo.com/en/docs 的 weather_code 表。
 */
fun Int.toWeatherCondition(): WeatherCondition = when (this) {
    0 -> WeatherCondition.CLEAR
    1, 2 -> WeatherCondition.PARTLY_CLOUDY
    3 -> WeatherCondition.CLOUDY
    45, 48 -> WeatherCondition.FOG
    51, 53, 55 -> WeatherCondition.DRIZZLE
    56, 57 -> WeatherCondition.FREEZING_RAIN
    61, 63, 65 -> WeatherCondition.RAIN
    66, 67 -> WeatherCondition.FREEZING_RAIN
    71, 73, 75 -> WeatherCondition.SNOW
    77 -> WeatherCondition.SNOW_GRAINS
    80, 81, 82 -> WeatherCondition.RAIN_SHOWERS
    85, 86 -> WeatherCondition.SNOW_SHOWERS
    95 -> WeatherCondition.THUNDERSTORM
    96, 99 -> WeatherCondition.THUNDERSTORM_HAIL
    else -> WeatherCondition.UNKNOWN
}
