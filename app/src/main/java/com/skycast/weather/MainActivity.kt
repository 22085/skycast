package com.skycast.weather

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.skycast.weather.ui.search.SearchActivity
import com.skycast.weather.ui.weather.WeatherScreen
import com.skycast.weather.ui.weather.WeatherViewModel
import com.skycast.weather.ui.weather.WeatherViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: WeatherViewModel by viewModels {
        WeatherViewModelFactory((application as SkycastApplication).container)
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        // 无论授权结果如何都尝试刷新；无权限时 ViewModel 会给出提示。
        if (granted.values.any { it }) viewModel.useCurrentLocation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    WeatherScreen(
                        state = viewModel.uiState.collectAsStateWithLifecycle(),
                        isRefreshing = viewModel.isRefreshing.collectAsStateWithLifecycle(),
                        onRefresh = viewModel::refresh,
                        onSearchClick = ::openSearch,
                    )
                }
            }
        }
    }

    private fun openSearch() {
        startActivity(Intent(this, SearchActivity::class.java))
    }

    /** 首次进入若没有保存地点，请求定位权限。 */
    override fun onStart() {
        super.onStart()
        if (!hasLocationPermission()) {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    private fun hasLocationPermission(): Boolean =
        checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
}
