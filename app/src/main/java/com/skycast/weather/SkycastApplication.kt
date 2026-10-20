package com.skycast.weather

import android.app.Application
import androidx.work.Configuration
import com.skycast.weather.data.location.LocationProvider
import com.skycast.weather.data.prefs.SettingsStore
import com.skycast.weather.data.repository.WeatherRepository
import com.skycast.weather.widget.WidgetUpdateWorker

class SkycastApplication : Application(), Configuration.Provider {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        WidgetUpdateWorker.schedule(this)
    }

    /**
     * 提供 WorkManager 配置。
     *
     * Manifest 中移除了默认的 WorkManagerInitializer，改为按需初始化，
     * 因此必须在这里给出配置，否则 WorkManager.getInstance() 会抛异常。
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}

/** 手动依赖容器。 */
class AppContainer(application: Application) {
    val settingsStore: SettingsStore by lazy { SettingsStore(application) }
    val locationProvider: LocationProvider by lazy { LocationProvider(application) }
    val weatherRepository: WeatherRepository by lazy { WeatherRepository() }
}