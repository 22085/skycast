package com.skycast.weather.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResponse(
    val results: List<GeoResult>? = null,
)

@Serializable
data class GeoResult(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val countryCode: String? = null,
    val admin1: String? = null,
    val admin2: String? = null,
    val timezone: String? = null,
    val population: Int? = null,
) {
    /** 展示用名称，例如「上海 · 上海市」 */
    val displayName: String
        get() = buildList {
            add(name)
            add(admin1 ?: admin2 ?: country ?: "")
        }.filter { it.isNotBlank() }.joinToString(" · ")
}
