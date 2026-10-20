package com.skycast.weather

import android.app.Application
import com.skycast.weather.data.location.LocationProvider
import com.skycast.weather.data.prefs.SettingsStore
import com.skycast.weather.data.repository.WeatherRepository

class SkycastApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** 手动依赖容器。 */
class AppContainer(application: Application) {
    val settingsStore: SettingsStore by lazy { SettingsStore(application) }
    val locationProvider: LocationProvider by lazy { LocationProvider(application) }
    val weatherRepository: WeatherRepository by lazy { WeatherRepository() }
}
