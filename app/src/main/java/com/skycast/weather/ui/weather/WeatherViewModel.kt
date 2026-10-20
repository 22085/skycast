package com.skycast.weather.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skycast.weather.data.location.LocationProvider
import com.skycast.weather.data.prefs.SavedLocation
import com.skycast.weather.data.prefs.SettingsStore
import com.skycast.weather.data.repository.CitySuggestion
import com.skycast.weather.data.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 天气页 ViewModel。
 *
 * 以 [SettingsStore.savedLocation] 为唯一数据源：搜索页保存新地点后，
 * 这里的 collect 会自动触发重新加载，不需要额外的回调或广播。
 */
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

    private var fetchJob: Job? = null
    private var locationRequested = false

    init {
        viewModelScope.launch {
            settingsStore.savedLocation
                .distinctUntilChanged()
                .collect { saved ->
                    if (saved != null) {
                        fetch(saved)
                    } else if (!locationRequested) {
                        // 首次启动且没有保存地点：尝试定位
                        requestCurrentLocation()
                    }
                }
        }
    }

    /** 手动刷新当前已保存的地点。 */
    fun refresh() {
        viewModelScope.launch {
            val saved = settingsStore.savedLocation.first()
            if (saved == null) {
                requestCurrentLocation()
            } else {
                fetch(saved)
            }
        }
    }

    /** 用户点击「使用当前位置」。 */
    fun useCurrentLocation() {
        viewModelScope.launch { requestCurrentLocation() }
    }

    private suspend fun requestCurrentLocation() {
        locationRequested = true

        if (!locationProvider.hasPermission()) {
            _uiState.value = WeatherUiState.Error(
                message = "需要定位权限才能获取当前位置天气",
            )
            return
        }

        _isRefreshing.value = true
        _uiState.value = WeatherUiState.Loading

        locationProvider.currentLocation()
            .onSuccess { (lat, lon) ->
                // 写入 DataStore 后会经上面的 collect 触发 fetch
                settingsStore.saveLocation(
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
                _uiState.value = WeatherUiState.Error(
                    message = "无法获取当前位置，请检查定位权限是否已开启",
                )
            }

        _isRefreshing.value = false
    }

    /** 拉取指定地点的天气并更新状态。纯读操作，不写 DataStore。 */
    private suspend fun fetch(location: SavedLocation) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _isRefreshing.value = true
            // 已有数据时不闪回 Loading，避免刷新时界面跳动
            if (_uiState.value !is WeatherUiState.Success) {
                _uiState.value = WeatherUiState.Loading
            }

            repository.getWeather(
                latitude = location.latitude,
                longitude = location.longitude,
                locationName = location.name,
                regionName = location.region,
                isCurrentLocation = location.isCurrent,
            )
                .onSuccess { weather ->
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

            _isRefreshing.value = false
        }
    }

    /** 搜索页选中城市后调用。 */
    fun selectCity(suggestion: CitySuggestion) {
        viewModelScope.launch {
            settingsStore.saveLocation(
                SavedLocation(
                    name = suggestion.name,
                    region = suggestion.country,
                    latitude = suggestion.latitude,
                    longitude = suggestion.longitude,
                    isCurrent = false,
                ),
            )
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