package com.todaywork.app.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "CalendarWidget"

class CalendarWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = CalendarWidget()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // 위젯 첫 배치 또는 기기 재시작 시 즉시 갱신
    // onAppWidgetOptionsChanged(크기 변경)는 SizeMode.Exact가 자체적으로 감지해
    // 재구성하므로 별도로 호출하지 않는다. 중복 호출 시 리사이즈 중 여러 업데이트가
    // 경합(race)하면서 낡은 스냅샷이 최종 렌더로 남는 문제가 있었다.
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scope.launch {
            try {
                val manager = GlanceAppWidgetManager(context)
                val ids = manager.getGlanceIds(CalendarWidget::class.java)
                Log.d(TAG, "onEnabled: updating ${ids.size} widget(s)")
                ids.forEach { CalendarWidget().update(context, it) }
            } catch (e: Exception) {
                Log.e(TAG, "onEnabled update failed", e)
            }
        }
    }
}
