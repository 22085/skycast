package com.skycast.weather.widget

import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.material3.MaterialTheme
import androidx.glance.material3.Text
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import androidx.glance.unit.dp
import com.skycast.weather.MainActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.widgetStore by preferencesDataStore(name = "widget_weather")

object SkycastWidget : GlanceAppWidget() {

    private val KEY_TEMP = stringPreferencesKey("temp")
    private val KEY_DESC = stringPreferencesKey("desc")
    private val KEY_LOC = stringPreferencesKey("loc")

    override suspend fun provideGlance(context: Context, glanceId: GlanceId) {
        val prefs = runBlocking { context.widgetStore.data.first() }
        val temp = prefs[KEY_TEMP] ?: "--°"
        val desc = prefs[KEY_DESC] ?: "下拉刷新"
        val loc = prefs[KEY_LOC] ?: "Skycast"

        provideContent {
            MaterialTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .appWidgetBackground(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = loc,
                        style = TextStyle(fontWeight = FontWeight.Medium),
                    )
                    Text(
                        text = temp,
                        style = TextStyle(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = desc,
                        style = TextStyle(
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                }
            }
        }
    }
}

class SkycastWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SkycastWidget
}

private fun GlanceModifier.appWidgetBackground() = this
