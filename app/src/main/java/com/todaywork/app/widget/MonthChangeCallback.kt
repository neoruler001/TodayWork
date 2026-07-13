package com.todaywork.app.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.todaywork.app.data.datastore.shiftWidgetMonth

private const val TAG = "CalendarWidget"

class MonthChangeCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val delta = parameters[deltaKey]
        if (delta == null) {
            Log.e(TAG, "MonthChangeCallback: deltaKey missing in parameters")
            return
        }
        try {
            context.shiftWidgetMonth(delta)
            CalendarWidget().update(context, glanceId)
            Log.d(TAG, "MonthChangeCallback: shifted by $delta, widget updated")
        } catch (e: Exception) {
            Log.e(TAG, "MonthChangeCallback failed (delta=$delta)", e)
        }
    }

    companion object {
        val deltaKey = ActionParameters.Key<Int>("month_delta")
    }
}
