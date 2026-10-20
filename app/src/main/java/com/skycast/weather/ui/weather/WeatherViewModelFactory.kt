package com.skycast.weather.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.skycast.weather.AppContainer
import com.skycast.weather.data.repository.WeatherRepository

/** 手动 ViewModel 工厂，避免引入 Hilt。 */
class WeatherViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            "未知 ViewModel: ${modelClass.name}"
        }
        return WeatherViewModel(
            repository = container.weatherRepository as WeatherRepository,
            settingsStore = container.settingsStore,
            locationProvider = container.locationProvider,
        ) as T
    }
}
