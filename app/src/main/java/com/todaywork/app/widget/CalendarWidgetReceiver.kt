package com.todaywork.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Bundle
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CalendarWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = CalendarWidget()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // 위젯 첫 배치 또는 기기 재시작 시 즉시 갱신
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        forceUpdate(context)
    }

    // 크기 변경 시 즉시 재렌더 (SizeMode.Responsive가 자동 처리하지만 명시적으로 보장)
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        forceUpdate(context)
    }

    private fun forceUpdate(context: Context) {
        scope.launch {
            try {
                val manager = GlanceAppWidgetManager(context)
                val ids = manager.getGlanceIds(CalendarWidget::class.java)
                ids.forEach { CalendarWidget().update(context, it) }
            } catch (_: Exception) {}
        }
    }
}
