package com.todaywork.app.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.todaywork.app.data.datastore.resetWidgetMonthToToday

private const val TAG = "CalendarWidget"

class RefreshCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        try {
            context.resetWidgetMonthToToday()
            CalendarWidget().update(context, glanceId)
            Log.d(TAG, "RefreshCallback: moved to current month, widget updated")
        } catch (e: Exception) {
            Log.e(TAG, "RefreshCallback failed", e)
        }
    }
}
