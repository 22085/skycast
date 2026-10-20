package com.skycast.weather.ui.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skycast.weather.R
import com.skycast.weather.domain.model.AirQuality
import com.skycast.weather.domain.model.DailyWeather
import com.skycast.weather.domain.model.HourlyWeather
import com.skycast.weather.domain.model.Weather
import com.skycast.weather.domain.model.WeatherCondition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    state: WeatherUiState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (state) {
                            is WeatherUiState.Success -> state.weather.locationName
                            else -> stringResource(R.string.app_name)
                        },
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search_title),
                        )
                    }
                    IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "刷新",
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (state) {
                is WeatherUiState.Loading -> LoadingContent()
                is WeatherUiState.Error -> ErrorContent(
                    message = state.message,
                    onRetry = onRefresh,
                )
                is WeatherUiState.Success -> WeatherContent(state.weather)
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        androidx.compose.material3.Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun WeatherContent(weather: Weather) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { CurrentCard(weather) }
        item { HourlyCard(weather.hourly) }
        item { DailyCard(weather.daily) }
        item { DetailsCard(weather) }
        item {
            Text(
                text = stringResource(R.string.data_source),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CurrentCard(weather: Weather) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (weather.regionName != null) {
                Text(
                    text = weather.regionName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = "${weather.current.temperatureC}°",
                fontSize = 72.sp,
                fontWeight = FontWeight.Light,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(weather.current.visual.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(weather.current.visual.descriptionRes),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.feels_like,
                    weather.current.feelsLikeC,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HourlyCard(hourly: List<HourlyWeather>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.hourly_forecast),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(hourly, key = { it.timeIso }) { hour ->
                    HourItem(hour)
                }
            }
        }
    }
}

@Composable
private fun HourItem(hour: HourlyWeather) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = hour.hourLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Icon(
            painter = painterResource(hour.visual.iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "${hour.temperatureC}°",
            style = MaterialTheme.typography.titleSmall,
        )
        if (hour.precipitationProbability != null && hour.precipitationProbability > 0) {
            Text(
                text = "${hour.precipitationProbability}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun DailyCard(daily: List<DailyWeather>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.daily_forecast),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            daily.forEachIndexed { index, day ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (index == 0) {
                            stringResource(R.string.today)
                        } else {
                            day.dayLabel
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(72.dp),
                    )
                    Icon(
                        painter = painterResource(day.visual.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    if (day.precipitationProbability != null && day.precipitationProbability > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${day.precipitationProbability}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(36.dp),
                        )
                    } else {
                        Spacer(modifier = Modifier.width(44.dp))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${day.minTempC}°",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${day.maxTempC}°",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsCard(weather: Weather) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "详细信息",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow(stringResource(R.string.humidity), "${weather.current.humidityPercent}%")
            DetailRow(
                stringResource(R.string.wind),
                "${windDirectionText(weather.current.windDirectionDeg)} " +
                    "${"%.0f".format(weather.current.windSpeedKmh)} km/h",
            )
            weather.current.pressureHpa?.let {
                DetailRow(stringResource(R.string.pressure), "${"%.0f".format(it)} hPa")
            }
            weather.current.cloudCoverPercent?.let {
                DetailRow("云量", "$it%")
            }
            weather.airQuality?.let { aq ->
                DetailRow(
                    stringResource(R.string.air_quality),
                    buildString {
                        aq.displayAqi?.let { append("$it ") }
                        append(stringResource(aq.levelRes))
                        aq.pm25?.let { append(" · PM2.5 ${"%.0f".format(it)}") }
                    },
                )
            }
            weather.daily.firstOrNull()?.let { today ->
                today.sunriseIso?.let { DetailRow(stringResource(R.string.sunrise), it.timeLabel()) }
                today.sunsetIso?.let { DetailRow(stringResource(R.string.sunset), it.timeLabel()) }
                today.uvIndexMax?.let {
                    DetailRow(stringResource(R.string.uv_index), "${"%.0f".format(it)}")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun windDirectionText(deg: Int): String = when (deg) {
    in 0..22, in 338..360 -> "北"
    in 23..67 -> "东北"
    in 68..112 -> "东"
    in 113..157 -> "东南"
    in 158..202 -> "南"
    in 203..247 -> "西南"
    in 248..292 -> "西"
    in 293..337 -> "西北"
    else -> "—"
}

private fun String.timeLabel(): String = if (length >= 16) substring(11, 16) else this

@Preview(showBackground = true)
@Composable
private fun WeatherScreenPreview() {
    val weather = Weather(
        locationName = "上海",
        regionName = "上海市",
        latitude = 31.23,
        longitude = 121.47,
        timezone = "Asia/Shanghai",
        current = com.skycast.weather.domain.model.CurrentWeather(
            temperatureC = 24,
            feelsLikeC = 26,
            condition = WeatherCondition.PARTLY_CLOUDY,
            isDay = true,
            humidityPercent = 68,
            windSpeedKmh = 12.5,
            windDirectionDeg = 135,
            pressureHpa = 1013.2,
            cloudCoverPercent = 40,
            observedAt = "2026-10-20T18:15",
        ),
        hourly = listOf(
            HourlyWeather(
                timeIso = "2026-10-20T18:00",
                hourLabel = "18:00",
                temperatureC = 24,
                condition = WeatherCondition.PARTLY_CLOUDY,
                isDay = true,
                precipitationProbability = 10,
            ),
        ),
        daily = listOf(
            DailyWeather(
                dateIso = "2026-10-20",
                dayLabel = "10月20日 周二",
                condition = WeatherCondition.PARTLY_CLOUDY,
                maxTempC = 27,
                minTempC = 20,
                sunriseIso = "2026-10-20T06:00",
                sunsetIso = "2026-10-20T17:17",
                precipitationProbability = 15,
                windSpeedMaxKmh = 18.0,
                uvIndexMax = 4.2,
            ),
        ),
        airQuality = AirQuality(
            europeanAqi = 42,
            usAqi = 58,
            pm25 = 12.4,
            pm10 = 25.0,
            ozone = 68.0,
        ),
        isCurrentLocation = true,
    )
    MaterialTheme {
        WeatherScreen(
            state = WeatherUiState.Success(weather, "更新于 18:20"),
            isRefreshing = false,
            onRefresh = {},
            onSearchClick = {},
        )
    }
}
