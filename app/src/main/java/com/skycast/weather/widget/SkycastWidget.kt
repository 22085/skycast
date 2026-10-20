package com.skycast.weather.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.material3.ColorProviders
import androidx.glance.material3.MaterialTheme
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.dp
import androidx.glance.unit.sp
import com.skycast.weather.MainActivity
import kotlinx.coroutines.flow.first

/**
 * 桌面小组件。点击整块区域打开主界面。
 */
class SkycastWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = WidgetStore.snapshotFlow(context).first()

        provideContent {
            WidgetContent(snapshot)
        }
    }

    @Composable
    private fun WidgetContent(snapshot: WidgetSnapshot) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(android.graphics.Color.parseColor("#0F2027")))
                .padding(14.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = snapshot.location,
                style = TextStyle(
                    color = ColorProvider(android.graphics.Color.WHITE),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = snapshot.temperature,
                style = TextStyle(
                    color = ColorProvider(android.graphics.Color.WHITE),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = snapshot.condition,
                style = TextStyle(
                    color = ColorProvider(android.graphics.Color.parseColor("#B0BEC5")),
                    fontSize = 13.sp,
                ),
            )
            if (snapshot.highLow.isNotBlank()) {
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = snapshot.highLow,
                    style = TextStyle(
                        color = ColorProvider(android.graphics.Color.parseColor("#B0BEC5")),
                        fontSize = 12.sp,
                    ),
                )
            }
        }
    }
}

/**
 * Widget provider。`updatePeriodMillis` 受系统限制最低 30 分钟，
 * 因此更频繁的刷新交给 [WidgetUpdateWorker]。
 */
class SkycastWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SkycastWidget()
}