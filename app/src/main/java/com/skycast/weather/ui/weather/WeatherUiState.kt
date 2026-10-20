package com.skycast.weather.ui.weather

import com.skycast.weather.domain.model.Weather

/** 天气页 UI 状态。 */
sealed interface WeatherUiState {
    data object Loading : WeatherUiState

    data class Success(
        val weather: Weather,
        val lastUpdatedLabel: String,
    ) : WeatherUiState

    data class Error(
        val message: String,
        val canRetry: Boolean = true,
    ) : WeatherUiState
}
