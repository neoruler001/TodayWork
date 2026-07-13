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
import com.todaywork.app.data.datastore.WidgetSettings
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

    // Exact: 실제 위젯 크기로 렌더링 → LocalSize.current = 실제 크기
    override val sizeMode = SizeMode.Exact

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

        val days             = repo.getDayInfosForMonth(year, month)
        val firstDay         = LocalDate.of(year, month, 1)
        val startPad         = firstDay.dayOfWeek.value % 7   // 일=0 ~ 토=6
        val rows             = (startPad + days.size + 6) / 7
        val prevMonthLastDay = firstDay.minusMonths(1).lengthOfMonth()
        val prevMonth        = if (month == 1) 12 else month - 1
        val nextMonth        = if (month == 12) 1 else month + 1

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
    settings: WidgetSettings,
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
    val size = LocalSize.current

    val bgColorValue = when (settings.bgColor) {
        "black" -> Color(0xFF1C1C1C)
        "gray"  -> Color(0xFFF5F5F5)
        "blue"  -> Color(0xFFE3F2FD)
        else    -> Color.White
    }
    val bgFinal       = bgColorValue.copy(alpha = settings.bgAlpha.coerceIn(0f, 1f))
    val isNight       = settings.bgColor == "black"
    val textPrimary   = if (isNight) Color(0xFFE0E0E0) else Color(0xFF212121)
    val textSecondary = if (isNight) Color(0xFF9E9E9E) else Color(0xFF757575)
    val textDim       = if (isNight) Color(0xFF555555) else Color(0xFFBBBBBB)
    val dividerColor  = if (isNight) Color(0xFF424242) else Color(0xFFE0E0E0)

    val large       = settings.fontSizeLarge
    val headerSp    = if (large) 24.sp else 20.sp
    val weekDaySp   = if (large) 15.sp else 13.sp
    val dateSp      = when (settings.dateSize) { 1 -> 12.sp; 3 -> 18.sp; else -> 15.sp }
    val badgeDp     = when (settings.workSize) { 1 -> 24.dp; 3 -> 36.dp; else -> 30.dp }
    val badgeTextSp = when (settings.workSize) { 1 -> 11.sp; 3 -> 16.sp; else -> 13.sp }
    val memoSp      = when (settings.memoSize) { 1 -> 9.sp;  3 -> 13.sp; else -> 11.sp }

    // 행 높이 명시 계산 (defaultWeight가 구형 기기에서 불안정)
    // 헤더 버튼 터치 영역이 44dp이므로 헤더 행은 반드시 그 이상이어야 함
    // (모자라면 버튼이 아래 요일 행까지 겹쳐 그려져 탭이 엉뚱한 곳으로 감)
    val headerRowH  = if (large) 52.dp else 48.dp
    val weekRowH    = if (large) 34.dp else 28.dp
    val divH        = if (settings.showDivider) 1.dp else 0.dp
    val fixedH      = headerRowH + weekRowH + divH * (rows + 1).toFloat()
    val calH        = size.height - fixedH - 12.dp  // 12dp = 위아래 padding
    val rowH        = if (rows > 0 && calH > 10.dp) (calH / rows).coerceAtLeast(28.dp)
                      else 40.dp

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

            // ── 헤더 ────────────────────────────────────────────
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(headerRowH)
                    .padding(bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 설정 버튼 (터치 영역 44dp)
                Box(
                    modifier = GlanceModifier
                        .size(44.dp)
                        .clickable(actionStartActivity(settingsIntent)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_w_settings),
                        contentDescription = "설정",
                        modifier = GlanceModifier.size(26.dp)
                    )
                }

                // 중앙 그룹: 이전달 · 년월 · 다음달 (가운데 정렬)
                Row(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = GlanceModifier
                            .size(44.dp)
                            .clickable(
                                actionRunCallback<MonthChangeCallback>(
                                    actionParametersOf(MonthChangeCallback.deltaKey to -1)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_w_prev),
                            contentDescription = "이전달",
                            modifier = GlanceModifier.size(26.dp)
                        )
                    }

                    Text(
                        text = "$year. ${month.toString().padStart(2, '0')}",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize   = headerSp,
                            color      = ColorProvider(day = textPrimary, night = textPrimary)
                        )
                    )

                    Box(
                        modifier = GlanceModifier
                            .size(44.dp)
                            .clickable(
                                actionRunCallback<MonthChangeCallback>(
                                    actionParametersOf(MonthChangeCallback.deltaKey to 1)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_w_next),
                            contentDescription = "다음달",
                            modifier = GlanceModifier.size(26.dp)
                        )
                    }
                }

                // 새로고침 버튼: 오늘이 포함된 월로 이동
                Box(
                    modifier = GlanceModifier
                        .size(44.dp)
                        .clickable(actionRunCallback<RefreshCallback>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_w_refresh),
                        contentDescription = "오늘로 이동",
                        modifier = GlanceModifier.size(26.dp)
                    )
                }
            }

            // ── 요일 헤더 ────────────────────────────────────────
            if (settings.showDivider)
                Spacer(modifier = GlanceModifier.fillMaxWidth().height(divH).background(dividerColor))

            Row(
                modifier = GlanceModifier.fillMaxWidth().height(weekRowH)
            ) {
                listOf("일", "월", "화", "수", "목", "금", "토").forEachIndexed { idx, lbl ->
                    // 그리드 열과 너비 일치: 구분선 동일하게 추가
                    if (idx > 0 && settings.showDivider)
                        Spacer(modifier = GlanceModifier.width(divH).fillMaxHeight().background(dividerColor))
                    val c = when (idx) {
                        0    -> Color(0xFFC62828)
                        6    -> Color(0xFF1565C0)
                        else -> textSecondary
                    }
                    Box(
                        modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = lbl,
                            style = TextStyle(
                                fontSize   = weekDaySp,
                                fontWeight = FontWeight.Bold,
                                color      = ColorProvider(day = c, night = c)
                            )
                        )
                    }
                }
            }

            if (settings.showDivider)
                Spacer(modifier = GlanceModifier.fillMaxWidth().height(divH).background(dividerColor))

            // ── 달력 그리드 ──────────────────────────────────────
            var dayIndex = 0
            for (r in 0 until rows) {
                // 행 높이를 명시적으로 지정
                Row(modifier = GlanceModifier.fillMaxWidth().height(rowH)) {
                    for (c in 0..6) {
                        if (c > 0 && settings.showDivider)
                            Spacer(modifier = GlanceModifier.width(divH).fillMaxHeight().background(dividerColor))

                        val cellIndex = r * 7 + c

                        when {
                            // 이전 달
                            cellIndex < startPad -> {
                                val d  = prevMonthLastDay - startPad + cellIndex + 1
                                val mm = prevMonth.toString().padStart(2, '0')
                                val dd = d.toString().padStart(2, '0')
                                Box(
                                    modifier = GlanceModifier
                                        .defaultWeight().fillMaxHeight()
                                        .padding(horizontal = 2.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.TopStart
                                ) {
                                    Text(
                                        text  = "$mm.$dd",
                                        style = TextStyle(
                                            fontSize = dateSp,
                                            color    = ColorProvider(day = textDim, night = textDim)
                                        )
                                    )
                                }
                            }

                            // 현재 달
                            dayIndex < days.size -> {
                                val dayInfo      = days[dayIndex]
                                val shift        = dayInfo.shiftType
                                val isToday      = dayInfo.date == today
                                val memos        = dayInfo.memos
                                val isHolidaySun = c == 0 || dayInfo.isHoliday
                                val isSat        = c == 6
                                val dateColor    = when {
                                    isHolidaySun -> Color(0xFFC62828)
                                    isSat        -> Color(0xFF1565C0)
                                    else         -> textPrimary
                                }
                                val dayNum  = dayInfo.date.dayOfMonth
                                val dateStr = if (dayNum == 1)
                                    "${month.toString().padStart(2, '0')}.01"
                                else
                                    dayNum.toString()
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
                                        .let { if (isToday) it.background(Color(0xFFBBDEFB)) else it }
                                        .clickable(actionStartActivity(confirmIntent))
                                ) {
                                    Column(
                                        modifier = GlanceModifier
                                            .fillMaxSize()
                                            .padding(horizontal = 2.dp, vertical = 2.dp),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            text  = dateLabel,
                                            style = TextStyle(
                                                fontSize   = dateSp,
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                color      = ColorProvider(day = dateColor, night = dateColor)
                                            ),
                                            maxLines = 1
                                        )

                                        if (settings.showLunar && dayInfo.lunarDay.isNotEmpty()) {
                                            Text(
                                                text  = dayInfo.lunarDay,
                                                style = TextStyle(
                                                    fontSize = memoSp,
                                                    color    = ColorProvider(day = textSecondary, night = textSecondary)
                                                )
                                            )
                                        }

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
                                                        color    = ColorProvider(
                                                            day   = Color(0xFF212121),
                                                            night = Color(0xFF212121)
                                                        )
                                                    ),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                                dayIndex++
                            }

                            // 다음 달
                            else -> {
                                val d  = cellIndex - startPad - days.size + 1
                                val mm = nextMonth.toString().padStart(2, '0')
                                val dd = d.toString().padStart(2, '0')
                                Box(
                                    modifier = GlanceModifier
                                        .defaultWeight().fillMaxHeight()
                                        .padding(horizontal = 2.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.TopStart
                                ) {
                                    Text(
                                        text  = "$mm.$dd",
                                        style = TextStyle(
                                            fontSize = dateSp,
                                            color    = ColorProvider(day = textDim, night = textDim)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (r < rows - 1 && settings.showDivider)
                    Spacer(modifier = GlanceModifier.fillMaxWidth().height(divH).background(dividerColor))
            }
        }
    }
}
