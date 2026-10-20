package com.skycast.weather.data.remote

import com.skycast.weather.data.remote.dto.AirQualityResponse
import com.skycast.weather.data.remote.dto.ForecastResponse
import com.skycast.weather.data.remote.dto.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo REST API。
 *
 * 数据基于 AGPL 许可的 NOAA/GFS 等公开模型，CC-BY 4.0 分发，商用免费且无需 API key。
 * 文档见 https://open-meteo.com/en/docs
 */
interface WeatherApi {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("hourly") hourly: String,
        @Query("daily") daily: String,
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 7,
        @Query("wind_speed_unit") windSpeedUnit: String = "kmh",
    ): ForecastResponse

    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("timezone") timezone: String = "auto",
    ): AirQualityResponse

    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "zh",
        @Query("format") format: String = "json",
    ): GeocodingResponse

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
        const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
        const val AIR_QUALITY_BASE_URL = "https://air-quality-api.open-meteo.com/"

        const val CURRENT_FIELDS =
            "temperature_2m,relative_humidity_2m,apparent_temperature,is_day," +
                "weather_code,surface_pressure,wind_speed_10m,wind_direction_10m,cloud_cover"

        const val HOURLY_FIELDS = "temperature_2m,weather_code,precipitation_probability,is_day"

        const val DAILY_FIELDS =
            "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset," +
                "precipitation_probability_max,wind_speed_10m_max,uv_index_max"

        const val AIR_QUALITY_FIELDS = "european_aqi,us_aqi,pm2_5,pm10,ozone"
    }
}
