package com.skycast.weather.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.skycast.weather.SkycastApplication
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * 定期拉取天气并刷新桌面小组件。
 *
 * WorkManager 的周期任务最小间隔是 15 分钟，这里取 1 小时以省电。
 */
class WidgetUpdateWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? SkycastApplication ?: return Result.failure()
        val container = app.container

        val saved = container.settingsStore.savedLocation.first()
            ?: return Result.success() // 还没选地点，等用户先打开 App

        val weather = container.weatherRepository.getWeather(
            latitude = saved.latitude,
            longitude = saved.longitude,
            locationName = saved.name,
            regionName = saved.region,
            isCurrentLocation = saved.isCurrent,
        ).getOrNull() ?: return Result.retry()

        val today = weather.daily.firstOrNull()
        val snapshot = WidgetSnapshot(
            location = weather.locationName,
            temperature = "${weather.current.temperatureC}°",
            condition = applicationContext.getString(weather.current.visual.descriptionRes),
            highLow = today?.let { "${it.maxTempC}° / ${it.minTempC}°" } ?: "",
            updatedAt = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
        )

        WidgetStore.save(applicationContext, snapshot)
        SkycastWidget().updateAll(applicationContext)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_NAME = "skycast_widget_refresh"

        /** 在 Application 启动时调用一次即可。 */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                1, TimeUnit.HOURS,
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        /** 用户主动刷新时立即跑一次。 */
        fun refreshNow(context: Context) {
            val request = androidx.work.OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}