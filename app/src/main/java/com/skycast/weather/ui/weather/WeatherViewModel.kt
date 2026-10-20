package com.skycast.weather.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skycast.weather.data.location.LocationProvider
import com.skycast.weather.data.prefs.SavedLocation
import com.skycast.weather.data.prefs.SettingsStore
import com.skycast.weather.data.repository.CitySuggestion
import com.skycast.weather.data.repository.WeatherRepository
import com.skycast.weather.domain.model.Weather
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val settingsStore: SettingsStore,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<CitySuggestion>>(emptyList())
    val searchResults: StateFlow<List<CitySuggestion>> = _searchResults.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var loadJob: Job? = null

    init {
        refresh()
    }

    /** 刷新当前已保存的地点。 */
    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isRefreshing.value = true
            _uiState.value = WeatherUiState.Loading
            try {
                val saved = settingsStore.savedLocation.first()
                if (saved == null) {
                    loadCurrentLocation()
                } else {
                    load(saved)
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /** 请求定位并加载定位地天气。 */
    fun useCurrentLocation() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isRefreshing.value = true
            _uiState.value = WeatherUiState.Loading
            try {
                loadCurrentLocation()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun loadCurrentLocation() {
        if (!locationProvider.hasPermission()) {
            _uiState.value = WeatherUiState.Error("需要定位权限才能获取当前位置天气")
            return
        }
        locationProvider.currentLocation()
            .onSuccess { (lat, lon) ->
                settingsStore.saveLocation(
                    SavedLocation(
                        name = "当前位置",
                        region = null,
                        latitude = lat,
                        longitude = lon,
                        isCurrent = true,
                    ),
                )
                load(
                    SavedLocation(
                        name = "当前位置",
                        region = null,
                        latitude = lat,
                        longitude = lon,
                        isCurrent = true,
                    ),
                )
            }
            .onFailure {
                _uiState.value = WeatherUiState.Error("无法获取当前位置，请检查定位权限")
            }
    }

    private suspend fun load(location: SavedLocation) {
        repository.getWeather(
            latitude = location.latitude,
            longitude = location.longitude,
            locationName = location.name,
            regionName = location.region,
            isCurrentLocation = location.isCurrent,
        )
            .onSuccess { weather ->
                settingsStore.saveLocation(location)
                _uiState.value = WeatherUiState.Success(
                    weather = weather,
                    lastUpdatedLabel = nowLabel(),
                )
            }
            .onFailure { e ->
                _uiState.value = WeatherUiState.Error(
                    message = e.message ?: "加载失败，请稍后重试",
                )
            }
    }

    fun selectCity(suggestion: CitySuggestion) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isRefreshing.value = true
            _uiState.value = WeatherUiState.Loading
            try {
                load(
                    SavedLocation(
                        name = suggestion.name,
                        region = suggestion.country,
                        latitude = suggestion.latitude,
                        longitude = suggestion.longitude,
                        isCurrent = false,
                    ),
                )
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            repository.searchCity(query)
                .onSuccess { _searchResults.value = it }
                .onFailure { _searchResults.value = emptyList() }
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
    }

    private fun nowLabel(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        return "更新于 ${formatter.format(LocalDateTime.now())}"
    }
}
