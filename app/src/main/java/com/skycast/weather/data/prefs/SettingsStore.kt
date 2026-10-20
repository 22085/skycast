package com.skycast.weather.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "skycast_settings")

/** 用户保存的地点。 */
data class SavedLocation(
    val name: String,
    val region: String?,
    val latitude: Double,
    val longitude: Double,
    val isCurrent: Boolean = false,
)

/**
 * 设置与已存地点。
 */
class SettingsStore(private val context: Context) {

    private object Keys {
        val LOCATION_NAME = stringPreferencesKey("location_name")
        val LOCATION_REGION = stringPreferencesKey("location_region")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val IS_CURRENT = stringPreferencesKey("is_current")
        val UNIT = stringPreferencesKey("temperature_unit")
    }

    val savedLocation: Flow<SavedLocation?> = context.dataStore.data.map { prefs ->
        val name = prefs[Keys.LOCATION_NAME] ?: return@map null
        val lat = prefs[Keys.LATITUDE] ?: return@map null
        val lon = prefs[Keys.LONGITUDE] ?: return@map null
        SavedLocation(
            name = name,
            region = prefs[Keys.LOCATION_REGION],
            latitude = lat,
            longitude = lon,
            isCurrent = prefs[Keys.IS_CURRENT] == "true",
        )
    }

    suspend fun saveLocation(location: SavedLocation) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LOCATION_NAME] = location.name
            prefs[Keys.LOCATION_REGION] = location.region ?: ""
            prefs[Keys.LATITUDE] = location.latitude
            prefs[Keys.LONGITUDE] = location.longitude
            prefs[Keys.IS_CURRENT] = location.isCurrent.toString()
        }
    }
}
