package com.todaywork.app.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.todaywork.app.data.datastore.shiftWidgetMonth

class MonthChangeCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val delta = parameters[deltaKey] ?: return
        context.shiftWidgetMonth(delta)
        CalendarWidget().update(context, glanceId)
    }

    companion object {
        val deltaKey = ActionParameters.Key<Int>("month_delta")
    }
}
