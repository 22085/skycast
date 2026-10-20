package com.skycast.weather.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 手动依赖装配。项目体量小，不引入 Hilt。
 *
 * 三个接口分别指向 Open-Meteo 的三个不同主机，因此各建一个 Retrofit 实例。
 */
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    private val contentType = "application/json".toMediaType()

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.NONE
                },
            )
            .build()
    }

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()

    /** 预报接口（api.open-meteo.com）。 */
    val weatherApi: WeatherApi by lazy {
        retrofit(WeatherApi.BASE_URL).create(WeatherApi::class.java)
    }

    /** 空气质量接口（air-quality-api.open-meteo.com）。 */
    val airQualityApi: WeatherApi by lazy {
        retrofit(WeatherApi.AIR_QUALITY_BASE_URL).create(WeatherApi::class.java)
    }

    /** 地理编码接口（geocoding-api.open-meteo.com）。 */
    val geocodingApi: WeatherApi by lazy {
        retrofit(WeatherApi.GEOCODING_BASE_URL).create(WeatherApi::class.java)
    }
}