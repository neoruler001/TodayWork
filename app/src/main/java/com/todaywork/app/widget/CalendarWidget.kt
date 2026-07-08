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
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProvider
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
                // 헤더
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$year. ${month.toString().padStart(2, '0')}",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ColorProvider(day = Color(0xFF1A1C19), night = Color(0xFFE2E3DE))
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    if (todayShift != null) {
                        val shiftColor = Color(todayShift.colorHex)
                        val textColor = if (todayShift.isLightBadge) Color(0xFF212121) else Color.White
                        if (todayShift.isWorkDay) {
                            Box(
                                modifier = GlanceModifier
                                    .size(32.dp)
                                    .background(shiftColor)
                                    .cornerRadius(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = todayShift.shortLabel,
                                    style = TextStyle(
                                        color = ColorProvider(day = textColor, night = textColor),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        } else {
                            Text(
                                text = todayShift.shortLabel,
                                style = TextStyle(
                                    color = ColorProvider(day = shiftColor, night = shiftColor),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }

                // 요일 헤더
                val weekDays = listOf("일", "월", "화", "수", "목", "금", "토")
                Row(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 3.dp)) {
                    weekDays.forEachIndexed { idx, d ->
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

                // 달력 그리드
                val firstDay = LocalDate.of(year, month, 1)
                val startPad = firstDay.dayOfWeek.value % 7
                val totalCells = startPad + days.size
                val rows = (totalCells + 6) / 7

                var dayIndex = 0
                for (r in 0 until rows) {
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight()
                            .padding(top = 1.dp)
                    ) {
                        for (c in 0..6) {
                            val cellIndex = r * 7 + c
                            if (cellIndex < startPad || dayIndex >= days.size) {
                                Box(modifier = GlanceModifier.defaultWeight()) {}
                            } else {
                                val dayInfo = days[dayIndex]
                                val shift = dayInfo.shiftType
                                val isToday = dayInfo.date == today
                                val dateStr = dayInfo.date.toString()
                                val memos = dayInfo.memos

                                val action = actionStartActivity<WidgetConfirmActivity>(
                                    actionParametersOf(
                                        ActionParameters.Key<String>("selected_date") to dateStr
                                    )
                                )

                                val isHolidayOrSun = c == 0 || dayInfo.isHoliday
                                val dateColor = when {
                                    isToday -> Color(0xFF1976D2)
                                    isHolidayOrSun -> Color(0xFFC62828)
                                    c == 6 -> Color(0xFF1565C0)
                                    else -> Color(0xFF1A1C19)
                                }

                                Column(
                                    modifier = GlanceModifier
                                        .defaultWeight()
                                        .fillMaxHeight()
                                        .clickable(action),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // 날짜
                                    Text(
                                        text = dayInfo.date.dayOfMonth.toString(),
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = ColorProvider(day = dateColor, night = dateColor)
                                        )
                                    )

                                    // 근무 배지
                                    if (shift != null) {
                                        val shiftColor = Color(shift.colorHex)
                                        val textColor = if (shift.isLightBadge) Color(0xFF212121) else Color.White
                                        if (shift.isWorkDay) {
                                            Box(
                                                modifier = GlanceModifier
                                                    .size(22.dp)
                                                    .background(shiftColor)
                                                    .cornerRadius(11.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = shift.shortLabel,
                                                    style = TextStyle(
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = ColorProvider(day = textColor, night = textColor)
                                                    )
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = shift.shortLabel,
                                                style = TextStyle(
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ColorProvider(day = shiftColor, night = shiftColor)
                                                )
                                            )
                                        }
                                    }

                                    // 첫 번째 메모 바
                                    if (memos.isNotEmpty()) {
                                        val memo = memos.first()
                                        val memoColor = Color(memo.colorHex)
                                        val memoText = if (memos.size > 1) "${memo.title}+" else memo.title
                                        Box(
                                            modifier = GlanceModifier
                                                .fillMaxWidth()
                                                .background(memoColor)
                                                .cornerRadius(2.dp)
                                                .padding(horizontal = 1.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = memoText,
                                                style = TextStyle(
                                                    fontSize = 7.sp,
                                                    color = ColorProvider(day = Color.White, night = Color.White)
                                                ),
                                                maxLines = 1
                                            )
                                        }
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
