package com.skycast.weather.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_weather")

/** Widget 展示用的天气快照。 */
data class WidgetSnapshot(
    val location: String = "Skycast",
    val temperature: String = "--°",
    val condition: String = "打开 App 获取天气",
    val highLow: String = "",
    val updatedAt: String = "",
)

/**
 * Widget 快照的持久化。由 [WidgetUpdateWorker] 写入，由 Widget 读取。
 *
 * 不直接把 `AppContainer` 里的仓库塞进 Widget——Widget 运行在
 * 独立的 provider 进程里，只共享磁盘上的 DataStore。
 */
object WidgetStore {

    private val KEY_LOCATION = stringPreferencesKey("location")
    private val KEY_TEMP = stringPreferencesKey("temperature")
    private val KEY_CONDITION = stringPreferencesKey("condition")
    private val KEY_HIGH_LOW = stringPreferencesKey("high_low")
    private val KEY_UPDATED_AT = stringPreferencesKey("updated_at")

    fun snapshotFlow(context: Context): Flow<WidgetSnapshot> =
        context.widgetDataStore.data.map { prefs ->
            WidgetSnapshot(
                location = prefs[KEY_LOCATION] ?: "Skycast",
                temperature = prefs[KEY_TEMP] ?: "--°",
                condition = prefs[KEY_CONDITION] ?: "打开 App 获取天气",
                highLow = prefs[KEY_HIGH_LOW] ?: "",
                updatedAt = prefs[KEY_UPDATED_AT] ?: "",
            )
        }

    suspend fun save(context: Context, snapshot: WidgetSnapshot) {
        context.widgetDataStore.edit { prefs ->
            prefs[KEY_LOCATION] = snapshot.location
            prefs[KEY_TEMP] = snapshot.temperature
            prefs[KEY_CONDITION] = snapshot.condition
            prefs[KEY_HIGH_LOW] = snapshot.highLow
            prefs[KEY_UPDATED_AT] = snapshot.updatedAt
        }
    }
}