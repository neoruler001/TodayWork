package com.todaywork.app.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.todaywork.app.data.datastore.WidgetPreferences
import dagger.hilt.android.EntryPointAccessors
import java.time.LocalDate

class MonthChangeCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val delta = parameters[deltaKey] ?: return
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            CalendarWidget.WidgetEntryPoint::class.java
        )
        val prefs = entryPoint.widgetPreferences()
        prefs.update {
            val date = LocalDate.of(displayYear, displayMonth, 1).plusMonths(delta.toLong())
            copy(displayYear = date.year, displayMonth = date.monthValue)
        }
        CalendarWidget().update(context, glanceId)
    }

    companion object {
        val deltaKey = ActionParameters.Key<Int>("month_delta")
    }
}
