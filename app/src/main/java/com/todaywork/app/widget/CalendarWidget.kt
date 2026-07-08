package com.todaywork.app.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProvider // 필수 임포트
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.todaywork.app.data.repository.ShiftRepository
import com.todaywork.app.ui.WidgetConfirmActivity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate

class CalendarWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun shiftRepository(): ShiftRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val shiftRepo = entryPoint.shiftRepository()
        
        val today = LocalDate.now()
        val year = today.year
        val month = today.monthValue

        val days = shiftRepo.getDayInfosForMonth(year, month)
        val todayShift = days.find { it.date == today }?.shiftType

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFFF9FBF7))
                    .padding(8.dp)
            ) {
                // ── 헤더 ──────────────────────────────────────
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${year}년 ${month}월",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ColorProvider(day = Color(0xFF1A1C19), night = Color(0xFFE2E3DE))
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    if (todayShift != null) {
                        Box(
                            modifier = GlanceModifier
                                .background(Color(todayShift.colorHex))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "오늘 ${todayShift.shortLabel}",
                                style = TextStyle(
                                    color = ColorProvider(day = Color.White, night = Color.White),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // ── 요일 헤더 ──────────────────────────────────
                val weekDays = listOf("일", "월", "화", "수", "목", "금", "토")
                Row(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 2.dp)) {
                    weekDays.forEachIndexed { idx, d ->
                        // 색상 로직 분리
                        val dayColor = when (idx) {
                            0 -> Color(0xFFC62828)
                            6 -> Color(0xFF1565C0)
                            else -> Color(0xFF757575)
                        }
                        Text(
                            text = d,
                            modifier = GlanceModifier.defaultWeight(),
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(day = dayColor, night = dayColor)
                            )
                        )
                    }
                }

                // ── 달력 그리드 ────────────────────────────────
                val firstDay = LocalDate.of(year, month, 1)
                val startPad = firstDay.dayOfWeek.value % 7
                val totalCells = startPad + days.size
                val rows = (totalCells + 6) / 7

                var dayIndex = 0
                for (r in 0 until rows) {
                    Row(modifier = GlanceModifier.fillMaxWidth().padding(top = 2.dp)) {
                        for (c in 0..6) {
                            val cellIndex = r * 7 + c
                            if (cellIndex < startPad || dayIndex >= days.size) {
                                Box(modifier = GlanceModifier.defaultWeight()) {}
                            } else {
                                val dayInfo = days[dayIndex]
                                val shift = dayInfo.shiftType
                                val isToday = dayInfo.date == today
                                val dateStr = dayInfo.date.toString()

                                val action = actionStartActivity<WidgetConfirmActivity>(
                                    actionParametersOf(ActionParameters.Key<String>("selected_date") to dateStr)
                                )

                                val dateColor = when {
                                    isToday -> Color(0xFF1976D2)
                                    c == 0 || dayInfo.isHoliday -> Color(0xFFC62828)
                                    c == 6 -> Color(0xFF1565C0)
                                    else -> Color(0xFF1A1C19)
                                }

                                Column(
                                    modifier = GlanceModifier.defaultWeight().clickable(action),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = dayInfo.date.dayOfMonth.toString(),
                                        style = TextStyle(
                                            fontSize = 11.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = ColorProvider(day = dateColor, night = dateColor)
                                        )
                                    )
                                    if (shift != null) {
                                        Box(
                                            modifier = GlanceModifier
                                                .fillMaxWidth()
                                                .background(Color(shift.colorHex))
                                                .padding(vertical = 1.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = shift.shortLabel,
                                                style = TextStyle(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ColorProvider(day = Color.White, night = Color.White)
                                                )
                                            )
                                        }
                                    } else {
                                        Box(modifier = GlanceModifier.fillMaxWidth().padding(vertical = 1.dp)) {}
                                    }
                                }
                                dayIndex++
                            }
                        }
                    }
                }
            }
        }
    }
}