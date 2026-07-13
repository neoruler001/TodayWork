package com.todaywork.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.todaywork.app.R
import com.todaywork.app.data.datastore.getWidgetSettings
import com.todaywork.app.data.model.DayInfo
import com.todaywork.app.data.repository.ShiftRepository
import com.todaywork.app.ui.WidgetConfirmActivity
import com.todaywork.app.ui.WidgetSettingsActivity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate

class CalendarWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun shiftRepository(): ShiftRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = EntryPointAccessors
            .fromApplication(context, WidgetEntryPoint::class.java)
            .shiftRepository()

        val settings = context.getWidgetSettings()
        val today    = LocalDate.now()
        val year     = settings.displayYear
        val month    = settings.displayMonth

        val days           = repo.getDayInfosForMonth(year, month)
        val firstDay       = LocalDate.of(year, month, 1)
        val startPad       = firstDay.dayOfWeek.value % 7  // 0=Sun
        val rows           = (startPad + days.size + 6) / 7
        val prevMonthLastDay = firstDay.minusMonths(1).lengthOfMonth()
        val prevMonth      = if (month == 1) 12 else month - 1
        val nextMonth      = if (month == 12) 1 else month + 1

        provideContent {
            WidgetContent(
                context          = context,
                settings         = settings,
                year             = year,
                month            = month,
                today            = today,
                days             = days,
                startPad         = startPad,
                rows             = rows,
                prevMonthLastDay = prevMonthLastDay,
                prevMonth        = prevMonth,
                nextMonth        = nextMonth,
            )
        }
    }
}

@Composable
private fun WidgetContent(
    context: Context,
    settings: com.todaywork.app.data.datastore.WidgetSettings,
    year: Int,
    month: Int,
    today: LocalDate,
    days: List<DayInfo>,
    startPad: Int,
    rows: Int,
    prevMonthLastDay: Int,
    prevMonth: Int,
    nextMonth: Int,
) {
    val bgColorValue = when (settings.bgColor) {
        "black" -> Color(0xFF1C1C1C)
        "gray"  -> Color(0xFFF5F5F5)
        "blue"  -> Color(0xFFE3F2FD)
        else    -> Color.White
    }
    val bgFinal        = bgColorValue.copy(alpha = settings.bgAlpha.coerceIn(0f, 1f))
    val isNight        = settings.bgColor == "black"
    val textPrimary    = if (isNight) Color(0xFFE0E0E0) else Color(0xFF212121)
    val textSecondary  = if (isNight) Color(0xFF9E9E9E) else Color(0xFF757575)
    val textDim        = if (isNight) Color(0xFF555555) else Color(0xFFBBBBBB)
    val dividerColor   = if (isNight) Color(0xFF424242) else Color(0xFFE0E0E0)

    val headerSp    = if (settings.fontSizeLarge) 17.sp else 15.sp
    val weekDaySp   = if (settings.fontSizeLarge) 12.sp else 10.sp
    val dateSp      = when (settings.dateSize) { 1 -> 10.sp; 3 -> 14.sp; else -> 12.sp }
    val badgeDp     = when (settings.workSize) { 1 -> 20.dp; 3 -> 28.dp; else -> 24.dp }
    val badgeTextSp = when (settings.workSize) { 1 -> 9.sp; 3 -> 13.sp; else -> 11.sp }
    val memoSp      = when (settings.memoSize) { 1 -> 7.sp; 3 -> 10.sp; else -> 8.sp }

    val settingsIntent = Intent(context, WidgetSettingsActivity::class.java)
        .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgFinal)
            .cornerRadius(16.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {

            // ── 헤더 ─────────────────────────────────────────────
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_w_settings),
                    contentDescription = "설정",
                    modifier = GlanceModifier.size(28.dp)
                        .clickable(actionStartActivity(settingsIntent))
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Image(
                    provider = ImageProvider(R.drawable.ic_w_prev),
                    contentDescription = "이전달",
                    modifier = GlanceModifier.size(28.dp)
                        .clickable(
                            actionRunCallback<MonthChangeCallback>(
                                actionParametersOf(MonthChangeCallback.deltaKey to -1)
                            )
                        )
                )
                Text(
                    text = "$year. ${month.toString().padStart(2, '0')}",
                    modifier = GlanceModifier.defaultWeight(),
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = headerSp,
                        color = ColorProvider(day = textPrimary, night = textPrimary)
                    )
                )
                Image(
                    provider = ImageProvider(R.drawable.ic_w_next),
                    contentDescription = "다음달",
                    modifier = GlanceModifier.size(28.dp)
                        .clickable(
                            actionRunCallback<MonthChangeCallback>(
                                actionParametersOf(MonthChangeCallback.deltaKey to 1)
                            )
                        )
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Image(
                    provider = ImageProvider(R.drawable.ic_w_refresh),
                    contentDescription = "새로고침",
                    modifier = GlanceModifier.size(28.dp)
                        .clickable(actionRunCallback<RefreshCallback>())
                )
            }

            // ── 요일 헤더 ─────────────────────────────────────────
            if (settings.showDivider) {
                Spacer(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(dividerColor))
            }
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp)
            ) {
                listOf("일", "월", "화", "수", "목", "금", "토").forEachIndexed { idx, label ->
                    val c = when (idx) {
                        0    -> Color(0xFFC62828)
                        6    -> Color(0xFF1565C0)
                        else -> textSecondary
                    }
                    Text(
                        text = label,
                        modifier = GlanceModifier.defaultWeight(),
                        style = TextStyle(
                            fontSize = weekDaySp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(day = c, night = c)
                        )
                    )
                }
            }
            if (settings.showDivider) {
                Spacer(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(dividerColor))
            }

            // ── 날짜 그리드 ───────────────────────────────────────
            var dayIndex = 0
            for (r in 0 until rows) {
                Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                    for (c in 0..6) {
                        if (c > 0 && settings.showDivider) {
                            Spacer(modifier = GlanceModifier.width(1.dp).fillMaxHeight().background(dividerColor))
                        }

                        val cellIndex = r * 7 + c

                        when {
                            // ── 이전 달 ─
                            cellIndex < startPad -> {
                                val d = prevMonthLastDay - startPad + cellIndex + 1
                                val mm = prevMonth.toString().padStart(2, '0')
                                val dd = d.toString().padStart(2, '0')
                                Box(
                                    modifier = GlanceModifier
                                        .defaultWeight().fillMaxHeight()
                                        .padding(2.dp),
                                    contentAlignment = Alignment.TopStart
                                ) {
                                    Text(
                                        text = "$mm.$dd",
                                        style = TextStyle(
                                            fontSize = dateSp,
                                            color = ColorProvider(day = textDim, night = textDim)
                                        )
                                    )
                                }
                            }

                            // ── 현재 달 ─
                            dayIndex < days.size -> {
                                val dayInfo     = days[dayIndex]
                                val shift       = dayInfo.shiftType
                                val isToday     = dayInfo.date == today
                                val memos       = dayInfo.memos
                                val isHolidaySun = c == 0 || dayInfo.isHoliday
                                val isSat        = c == 6

                                val dateColor = when {
                                    isHolidaySun -> Color(0xFFC62828)
                                    isSat        -> Color(0xFF1565C0)
                                    else         -> textPrimary
                                }

                                // 날짜 텍스트: 1일은 MM.01, 나머지는 일 번호
                                val dayNum  = dayInfo.date.dayOfMonth
                                val dateStr = if (dayNum == 1)
                                    "${month.toString().padStart(2, '0')}.01"
                                else
                                    dayNum.toString()

                                // 공휴일명 인라인
                                val dateLabel = if (dayInfo.holidayName.isNotEmpty())
                                    "$dateStr ${dayInfo.holidayName}"
                                else
                                    dateStr

                                val confirmIntent = Intent(context, WidgetConfirmActivity::class.java).apply {
                                    putExtra("selected_date", dayInfo.date.toString())
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }

                                Box(
                                    modifier = GlanceModifier
                                        .defaultWeight().fillMaxHeight()
                                        .let { if (isToday) it.background(Color(0xFFE3F2FD)) else it }
                                        .clickable(actionStartActivity(confirmIntent))
                                ) {
                                    Column(
                                        modifier = GlanceModifier
                                            .fillMaxSize()
                                            .padding(horizontal = 2.dp, vertical = 2.dp),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        // 날짜 (+ 공휴일명)
                                        Text(
                                            text  = dateLabel,
                                            style = TextStyle(
                                                fontSize   = dateSp,
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                color      = ColorProvider(day = dateColor, night = dateColor)
                                            ),
                                            maxLines = 1
                                        )

                                        // 음력
                                        if (settings.showLunar && dayInfo.lunarDay.isNotEmpty()) {
                                            Text(
                                                text  = dayInfo.lunarDay,
                                                style = TextStyle(
                                                    fontSize = 7.sp,
                                                    color    = ColorProvider(day = textSecondary, night = textSecondary)
                                                )
                                            )
                                        }

                                        // 근무 배지
                                        if (shift != null) {
                                            val shiftColor = Color(shift.colorHex)
                                            val badgeFg    = if (shift.isLightBadge) Color(0xFF212121) else Color.White
                                            if (shift.isWorkDay) {
                                                Box(
                                                    modifier = GlanceModifier.fillMaxWidth(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Box(
                                                        modifier = GlanceModifier
                                                            .size(badgeDp)
                                                            .background(shiftColor)
                                                            .cornerRadius(badgeDp / 2),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text  = shift.shortLabel,
                                                            style = TextStyle(
                                                                fontSize   = badgeTextSp,
                                                                fontWeight = FontWeight.Bold,
                                                                color      = ColorProvider(day = badgeFg, night = badgeFg)
                                                            )
                                                        )
                                                    }
                                                }
                                            } else {
                                                Text(
                                                    text  = shift.shortLabel,
                                                    style = TextStyle(
                                                        fontSize   = badgeTextSp,
                                                        fontWeight = FontWeight.Bold,
                                                        color      = ColorProvider(day = shiftColor, night = shiftColor)
                                                    )
                                                )
                                            }
                                        }

                                        // 메모 (최대 2개)
                                        memos.take(2).forEach { memo ->
                                            Box(
                                                modifier = GlanceModifier
                                                    .fillMaxWidth()
                                                    .background(Color(memo.colorHex))
                                                    .cornerRadius(2.dp)
                                                    .padding(horizontal = 1.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Text(
                                                    text     = memo.title,
                                                    style    = TextStyle(
                                                        fontSize = memoSp,
                                                        color    = ColorProvider(day = Color(0xFF212121), night = Color(0xFF212121))
                                                    ),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                                dayIndex++
                            }

                            // ── 다음 달 ─
                            else -> {
                                val d  = cellIndex - startPad - days.size + 1
                                val mm = nextMonth.toString().padStart(2, '0')
                                val dd = d.toString().padStart(2, '0')
                                Box(
                                    modifier = GlanceModifier
                                        .defaultWeight().fillMaxHeight()
                                        .padding(2.dp),
                                    contentAlignment = Alignment.TopStart
                                ) {
                                    Text(
                                        text = "$mm.$dd",
                                        style = TextStyle(
                                            fontSize = dateSp,
                                            color = ColorProvider(day = textDim, night = textDim)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (r < rows - 1 && settings.showDivider) {
                    Spacer(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(dividerColor))
                }
            }
        }
    }
}
