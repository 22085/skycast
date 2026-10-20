package com.skycast.weather.data.repository

import com.skycast.weather.data.remote.NetworkModule
import com.skycast.weather.data.remote.WeatherApi
import com.skycast.weather.domain.model.Weather
import java.time.ZoneId

/**
 * 天气数据仓库：串起预报、空气质量、地理编码三个接口。
 */
class WeatherRepository(
    private val api: WeatherApi = NetworkModule.weatherApi,
    private val airQualityApi: WeatherApi = NetworkModule.airQualityApi,
    private val geocodingApi: WeatherApi = NetworkModule.geocodingApi,
) {

    /**
     * 拉取指定坐标的完整天气。
     *
     * @param locationName 展示用地点名
     * @param regionName 上级行政区（可选）
     * @param isCurrentLocation 是否来自 GPS
     */
    suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        locationName: String,
        regionName: String?,
        isCurrentLocation: Boolean,
    ): Result<Weather> = runCatching {
        val forecast = api.getForecast(
            latitude = latitude,
            longitude = longitude,
            current = WeatherApi.CURRENT_FIELDS,
            hourly = WeatherApi.HOURLY_FIELDS,
            daily = WeatherApi.DAILY_FIELDS,
        )

        val current = forecast.current
            ?: error("响应缺少 current 字段")
        val hourly = forecast.hourly
            ?: error("响应缺少 hourly 字段")
        val daily = forecast.daily
            ?: error("响应缺少 daily 字段")

        val zone = runCatching { ZoneId.of(forecast.timezone) }
            .getOrDefault(ZoneId.systemDefault())

        val airQuality = runCatching {
            airQualityApi.getAirQuality(
                latitude = latitude,
                longitude = longitude,
                current = WeatherApi.AIR_QUALITY_FIELDS,
            ).let(WeatherMapper::mapAirQuality)
        }.getOrNull()

        Weather(
            locationName = locationName,
            regionName = regionName,
            latitude = forecast.latitude,
            longitude = forecast.longitude,
            timezone = forecast.timezone,
            current = WeatherMapper.mapCurrent(current),
            hourly = WeatherMapper.mapHourly(
                dto = hourly,
                zone = zone,
                limit = 24,
            ),
            daily = WeatherMapper.mapDaily(daily, zone),
            airQuality = airQuality,
            isCurrentLocation = isCurrentLocation,
        )
    }

    /**
     * 城市搜索。返回 (展示名, 纬度, 经度, 地区, 时区)。
     */
    suspend fun searchCity(
        query: String,
    ): Result<List<CitySuggestion>> = runCatching {
        if (query.isBlank()) {
            emptyList()
        } else {
            geocodingApi.searchCity(name = query.trim()).results
                ?.map {
                    CitySuggestion(
                        name = it.name,
                        displayName = it.displayName,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        country = it.country,
                        timezone = it.timezone,
                    )
                }
                ?: emptyList()
        }
    }
}

data class CitySuggestion(
    val name: String,
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    val timezone: String?,
)
