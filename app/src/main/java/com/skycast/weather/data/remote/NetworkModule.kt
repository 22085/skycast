package com.skycast.weather.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.KotlinSerializationConverter
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * 手动依赖装配。项目体量小，不引入 Hilt。
 */
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

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
        .addConverterFactory(KotlinSerializationConverter(json).asLenient())
        .build()

    val weatherApi: WeatherApi by lazy {
        retrofit(WeatherApi.BASE_URL).create(WeatherApi::class.java)
    }

    val airQualityApi: WeatherApi by lazy {
        retrofit(WeatherApi.AIR_QUALITY_BASE_URL).create(WeatherApi::class.java)
    }

    val geocodingApi: WeatherApi by lazy {
        retrofit(WeatherApi.GEOCODING_BASE_URL).create(WeatherApi::class.java)
    }
}
