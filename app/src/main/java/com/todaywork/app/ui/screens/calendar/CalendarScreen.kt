package com.todaywork.app.ui.screens.calendar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.todaywork.app.data.model.DayInfo
import com.todaywork.app.data.model.MemoItem
import com.todaywork.app.data.model.ShiftType
import com.todaywork.app.ui.theme.LunarText
import com.todaywork.app.ui.theme.WeekendSat
import com.todaywork.app.ui.theme.WeekendSun
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle as DateTextStyle
import java.util.Locale

private val MEMO_COLORS = listOf(
    0xFFFFD6E7L, 0xFFCDEFFFL, 0xFFFFF3C4L, 0xFFDDF7D8L, 0xFFF5E8FFL,
    0xFFCCD1FFL, 0xFFFFA9B0L, 0xFFFFDDA6L, 0xFFA8C8F0L, 0xFFB5EAD7L,
    0xFFFFE5CCL, 0xFFC9E4DEL, 0xFFF0D9FFL, 0xFFE0F7FAL, 0xFFFCE4ECL,
    0xFFE8EAF6L, 0xFFFFF9C4L, 0xFFDCEDC8L, 0xFFFFCDD2L,
)

private val MEMO_TEXT_DARK = Color(0xFF333333)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDaySheet by remember { mutableStateOf(false) }
    var showApplyPatternDialog by remember { mutableStateOf(false) }
    var calSwipeDelta by remember { mutableFloatStateOf(0f) }

    // Keep last valid DayInfo so exit animation works after clearSelection()
    var stableDayInfo by remember { mutableStateOf<DayInfo?>(null) }
    val currentDayInfo = remember(uiState.selectedDate, uiState.days) {
        uiState.selectedDate?.let { d -> uiState.days.find { it.date == d } }
    }
    if (currentDayInfo != null) stableDayInfo = currentDayInfo

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Main calendar ─────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            CalendarHeader(
                year = uiState.year,
                month = uiState.month,
                onPrev = {
                    showDaySheet = false
                    viewModel.goToPreviousMonth()
                },
                onNext = {
                    showDaySheet = false
                    viewModel.goToNextMonth()
                },
                onToday = viewModel::goToToday,
                onAddPattern = { showApplyPatternDialog = true }
            )
            WeekDayHeader()

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                CalendarGrid(
                    year = uiState.year,
                    month = uiState.month,
                    days = uiState.days,
                    selectedDate = uiState.selectedDate,
                    showLunar = uiState.showLunar,
                    showHoliday = uiState.showHoliday,
                    onDateClick = { date ->
                        viewModel.selectDate(date)
                        showDaySheet = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    when {
                                        calSwipeDelta < -80f -> {
                                            showDaySheet = false
                                            viewModel.goToNextMonth()
                                        }
                                        calSwipeDelta > 80f -> {
                                            showDaySheet = false
                                            viewModel.goToPreviousMonth()
                                        }
                                    }
                                    calSwipeDelta = 0f
                                },
                                onDragCancel = { calSwipeDelta = 0f },
                                onHorizontalDrag = { _, delta -> calSwipeDelta += delta }
                            )
                        }
                )
            }
        }

        // ── Full-screen day detail (slides in from right) ────
        AnimatedVisibility(
            visible = showDaySheet,
            enter = slideInHorizontally(animationSpec = tween(280)) { it },
            exit = slideOutHorizontally(animationSpec = tween(280)) { it },
            modifier = Modifier.fillMaxSize()
        ) {
            stableDayInfo?.let { info ->
                DayDetailFullScreen(
                    initialDayInfo = info,
                    uiState = uiState,
                    viewModel = viewModel,
                    onSaveShift = { date, shiftName, sh, sm, eh, em ->
                        viewModel.saveWorkRecord(date, shiftName, sh, sm, eh, em)
                    },
                    onResetShift = viewModel::resetDateToPattern,
                    onAddMemo = viewModel::addMemo,
                    onDeleteMemo = viewModel::deleteMemo,
                    onUpdateMemo = viewModel::updateMemo,
                    onDismiss = {
                        showDaySheet = false
                        viewModel.clearSelection()
                    }
                )
            }
        }
    }

    if (showApplyPatternDialog) {
        ApplyPatternDialog(
            patterns = uiState.patterns,
            onApply = { patternId, startDate, endDate ->
                viewModel.applyPatternToDateRange(patternId, startDate, endDate)
                showApplyPatternDialog = false
            },
            onDismiss = { showApplyPatternDialog = false }
        )
    }
}

// ── 달력 헤더 ─────────────────────────────────────────────────
@Composable
private fun CalendarHeader(
    year: Int, month: Int,
    onPrev: () -> Unit, onNext: () -> Unit, onToday: () -> Unit,
    onAddPattern: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "이전 달",
                tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            text = "$year. ${month.toString().padStart(2, '0')}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "다음 달",
                tint = MaterialTheme.colorScheme.onBackground)
        }
        FilledTonalButton(
            onClick = onToday,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text("오늘", style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.width(4.dp))
        FilledTonalButton(
            onClick = onAddPattern,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(2.dp))
            Text("패턴", style = MaterialTheme.typography.labelMedium)
        }
    }
}

// ── 요일 헤더 ─────────────────────────────────────────────────
@Composable
private fun WeekDayHeader() {
    val days = listOf("일", "월", "화", "수", "목", "금", "토")
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp)
    ) {
        days.forEachIndexed { idx, d ->
            Text(
                text = d,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                color = when (idx) {
                    0 -> WeekendSun
                    6 -> WeekendSat
                    else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

// ── 달력 그리드 (비스크롤) ─────────────────────────────────────
@Composable
private fun CalendarGrid(
    year: Int, month: Int,
    days: List<DayInfo>,
    selectedDate: LocalDate?,
    showLunar: Boolean,
    showHoliday: Boolean,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstDay = LocalDate.of(year, month, 1)
    val startPad = firstDay.dayOfWeek.value % 7
    val totalDays = firstDay.lengthOfMonth()
    val today = LocalDate.now()
    val numRows = (startPad + totalDays + 6) / 7

    Column(modifier = modifier) {
        for (row in 0 until numRows) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayIndex = cellIndex - startPad
                    if (dayIndex < 0 || dayIndex >= totalDays) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight())
                    } else {
                        val date = firstDay.plusDays(dayIndex.toLong())
                        val dayInfo = days.getOrNull(dayIndex)
                        CalendarCell(
                            dayInfo = dayInfo,
                            date = date,
                            isToday = date == today,
                            isSelected = date == selectedDate,
                            showLunar = showLunar,
                            showHoliday = showHoliday,
                            dow = date.dayOfWeek,
                            onClick = { onDateClick(date) },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    if (col < 6) {
                        VerticalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                            modifier = Modifier.fillMaxHeight()
                        )
                    }
                }
            }
            if (row < numRows - 1) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                )
            }
        }
    }
}

// ── 날짜 셀 ───────────────────────────────────────────────────
@Composable
private fun CalendarCell(
    dayInfo: DayInfo?,
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    showLunar: Boolean,
    showHoliday: Boolean,
    dow: DayOfWeek,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shift = dayInfo?.shiftType
    val isHoliday = dayInfo?.isHoliday == true
    val memos = dayInfo?.memos ?: emptyList()
    val holidayName = dayInfo?.holidayName.orEmpty()

    val dateColor = when {
        isHoliday || dow == DayOfWeek.SUNDAY -> WeekendSun
        dow == DayOfWeek.SATURDAY -> WeekendSat
        else -> MaterialTheme.colorScheme.onBackground
    }

    Box(
        modifier = modifier
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(
                            if (isToday) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        fontSize = 13.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) Color.White else dateColor
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 4.dp, end = 2.dp)
                ) {
                    if (showHoliday && isHoliday && dow != DayOfWeek.SUNDAY) {
                        Box(Modifier.size(4.dp).clip(CircleShape).background(WeekendSun))
                    }
                }
            }

            if (showLunar && dayInfo?.lunarDay?.isNotBlank() == true) {
                Text(
                    text = dayInfo.lunarDay,
                    fontSize = 8.sp,
                    color = LunarText,
                    maxLines = 1,
                    lineHeight = 9.sp
                )
            }

            Spacer(Modifier.weight(1f))

            if (shift != null) {
                if (shift.isWorkDay) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(shift.toColor()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shift.shortLabel,
                            fontSize = 15.sp,
                            color = shift.badgeTextColor(),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                } else {
                    Text(
                        text = shift.shortLabel,
                        fontSize = 17.sp,
                        color = shift.toColor(),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            if (showHoliday && isHoliday && holidayName.isNotBlank()) {
                Text(
                    text = holidayName,
                    fontSize = 7.sp,
                    color = WeekendSun,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .background(WeekendSun.copy(alpha = 0.1f))
                        .padding(horizontal = 2.dp)
                )
            }

            val hasMore = memos.size > 2
            val visibleMemos = memos.take(2)
            visibleMemos.forEachIndexed { idx, memo ->
                val isLast = idx == visibleMemos.lastIndex
                val displayText = if (isLast && hasMore) "${memo.title} more..." else memo.title
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 1.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(memo.colorHex))
                        .padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = displayText,
                        fontSize = 11.sp,
                        lineHeight = 11.sp,
                        color = MEMO_TEXT_DARK,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ── 풀스크린 날짜 상세 (오른쪽 슬라이드) ─────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDetailFullScreen(
    initialDayInfo: DayInfo,
    uiState: CalendarUiState,
    viewModel: CalendarViewModel,
    onSaveShift: (LocalDate, String, Int, Int, Int, Int) -> Unit,
    onResetShift: (LocalDate) -> Unit,
    onAddMemo: (LocalDate, String, Long, Int, Int, Boolean, Int) -> Unit,
    onDeleteMemo: (Long) -> Unit,
    onUpdateMemo: (Long, LocalDate, String, Long, Int, Int, Boolean, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var currentDate by remember { mutableStateOf(initialDayInfo.date) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showWorkEditDialog by remember { mutableStateOf(false) }
    var showMemoAddDialog by remember { mutableStateOf(false) }
    var editingMemo by remember { mutableStateOf<MemoItem?>(null) }
    var swipeDelta by remember { mutableFloatStateOf(0f) }

    // Load month when navigating to a different month
    LaunchedEffect(currentDate) {
        viewModel.loadMonthForDate(currentDate)
        viewModel.selectDate(currentDate)
    }

    val dayInfo = remember(currentDate, uiState.days) {
        uiState.days.find { it.date == currentDate }
            ?: DayInfo(date = currentDate, shiftType = null)
    }

    BackHandler { onDismiss() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Navigation header (swipeable) ────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, top = 4.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                when {
                                    swipeDelta < -100f -> currentDate = currentDate.plusDays(1)
                                    swipeDelta > 100f -> currentDate = currentDate.minusDays(1)
                                }
                                swipeDelta = 0f
                            },
                            onDragCancel = { swipeDelta = 0f },
                            onHorizontalDrag = { _, delta -> swipeDelta += delta }
                        )
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "닫기",
                        tint = MaterialTheme.colorScheme.onBackground)
                }
                IconButton(onClick = { currentDate = currentDate.minusDays(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "이전 날",
                        tint = MaterialTheme.colorScheme.onBackground)
                }

                // Animated date display
                AnimatedContent(
                    targetState = currentDate,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    modifier = Modifier.weight(1f),
                    label = "dateHeader"
                ) { date ->
                    val dowStr = date.dayOfWeek.getDisplayName(DateTextStyle.SHORT, Locale.KOREAN)
                    val isHol = dayInfo.isHoliday && dayInfo.date == date
                    val dayColor = when (date.dayOfWeek) {
                        DayOfWeek.SUNDAY -> WeekendSun
                        DayOfWeek.SATURDAY -> WeekendSat
                        else -> if (isHol) WeekendSun else MaterialTheme.colorScheme.onBackground
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${date.monthValue}월 ${date.dayOfMonth}일 ${dowStr}요일",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = dayColor,
                            textAlign = TextAlign.Center
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (dayInfo.lunarDay.isNotBlank() && dayInfo.date == date) {
                                Text("음 ${dayInfo.lunarDay}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LunarText)
                            }
                            if (dayInfo.isHoliday && dayInfo.date == date && dayInfo.holidayName.isNotBlank()) {
                                Text(dayInfo.holidayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WeekendSun)
                            }
                        }
                    }
                }

                IconButton(onClick = { currentDate = currentDate.plusDays(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "다음 날",
                        tint = MaterialTheme.colorScheme.onBackground)
                }
            }

            // ── Shift info row ────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val shift = dayInfo.shiftType
                if (shift != null) {
                    if (shift.isWorkDay) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(shift.toColor()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(shift.shortLabel, fontSize = 20.sp,
                                color = shift.badgeTextColor(), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                            Text(shift.shortLabel, fontSize = 26.sp,
                                color = shift.toColor(), fontWeight = FontWeight.Bold)
                        }
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(shift.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold)
                            if (dayInfo.isModified) {
                                Spacer(Modifier.width(6.dp))
                                Text("(수정됨)", fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                        if (dayInfo.startTime.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, null,
                                    modifier = Modifier.size(13.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Spacer(Modifier.width(3.dp))
                                Text("${dayInfo.startTime} - ${dayInfo.endTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                } else {
                    Text("근무 없음",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                }
            }

            HorizontalDivider()

            // ── Tabs ──────────────────────────────────────────
            TabRow(selectedTabIndex = selectedTab) {
                listOf("메모", "근무", "급여").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.labelLarge) }
                    )
                }
            }

            // ── Tab content (fills remaining space) ──────────
            when (selectedTab) {
                0 -> MemoTabContent(
                    memos = dayInfo.memos,
                    onAddClick = { showMemoAddDialog = true },
                    onDelete = onDeleteMemo,
                    onEdit = { editingMemo = it },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
                1 -> WorkTabContent(
                    dayInfo = dayInfo,
                    onEditClick = { showWorkEditDialog = true },
                    onReset = { onResetShift(currentDate) },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
                2 -> SalaryTabContent(modifier = Modifier.fillMaxWidth().weight(1f))
            }
        }
    }

    if (showWorkEditDialog) {
        WorkEditDialog(
            dayInfo = dayInfo,
            onSave = { shiftName, sh, sm, eh, em ->
                onSaveShift(currentDate, shiftName, sh, sm, eh, em)
                showWorkEditDialog = false
            },
            onDismiss = { showWorkEditDialog = false }
        )
    }

    if (showMemoAddDialog) {
        MemoEditDialog(
            onSave = { title, colorHex, startMin, endMin, isAllDay, reminder ->
                onAddMemo(currentDate, title, colorHex, startMin, endMin, isAllDay, reminder)
                showMemoAddDialog = false
            },
            onDismiss = { showMemoAddDialog = false }
        )
    }

    editingMemo?.let { memo ->
        MemoEditDialog(
            dialogTitle = "메모 수정",
            initialTitle = memo.title,
            initialColorHex = memo.colorHex,
            initialIsAllDay = memo.isAllDay,
            initialStartHour = if (memo.startTimeMinutes >= 0) memo.startTimeMinutes / 60 else 8,
            initialStartMin = if (memo.startTimeMinutes >= 0) memo.startTimeMinutes % 60 else 0,
            initialEndHour = if (memo.endTimeMinutes >= 0) memo.endTimeMinutes / 60 else 9,
            initialEndMin = if (memo.endTimeMinutes >= 0) memo.endTimeMinutes % 60 else 0,
            initialReminderMinutes = memo.reminderMinutes,
            onSave = { title, colorHex, startMin, endMin, isAllDay, reminder ->
                onUpdateMemo(memo.id, currentDate, title, colorHex, startMin, endMin, isAllDay, reminder)
                editingMemo = null
            },
            onDelete = {
                onDeleteMemo(memo.id)
                editingMemo = null
            },
            onDismiss = { editingMemo = null }
        )
    }
}

// ── 메모 탭 ───────────────────────────────────────────────────
@Composable
private fun MemoTabContent(
    memos: List<MemoItem>,
    onAddClick: () -> Unit,
    onDelete: (Long) -> Unit,
    onEdit: (MemoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(memos) { memo ->
            MemoCard(memo = memo, onDelete = { onDelete(memo.id) }, onEdit = { onEdit(memo) })
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAddClick)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AddCircleOutline, null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "새 메모",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun MemoCard(memo: MemoItem, onDelete: () -> Unit, onEdit: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(memo.colorHex))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(end = 32.dp)) {
            if (memo.timeDisplay.isNotBlank()) {
                Text(
                    text = memo.timeDisplay,
                    fontSize = 11.sp,
                    color = MEMO_TEXT_DARK.copy(alpha = 0.65f)
                )
            }
            Text(
                text = memo.title,
                fontSize = 15.sp,
                color = MEMO_TEXT_DARK,
                fontWeight = FontWeight.SemiBold
            )
        }
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.MoreVert, null, tint = MEMO_TEXT_DARK, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("수정") },
                    onClick = { showMenu = false; onEdit() },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, null)
                    }
                )
                DropdownMenuItem(
                    text = { Text("삭제") },
                    onClick = { showMenu = false; onDelete() },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                    }
                )
            }
        }
    }
}

// ── 근무 탭 ───────────────────────────────────────────────────
@Composable
private fun WorkTabContent(
    dayInfo: DayInfo,
    onEditClick: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onEditClick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("본근무 수정")
        }
        if (dayInfo.isModified) {
            OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("패턴으로 복원")
            }
        }
    }
}

// ── 급여 탭 (placeholder) ─────────────────────────────────────
@Composable
private fun SalaryTabContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            "급여 정보 준비 중",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

// ── 메모 추가/수정 다이얼로그 (t.jpeg / m.jpeg 레이아웃 완벽 매핑) ──────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemoEditDialog(
    dialogTitle: String = "새 메모",
    initialTitle: String = "",
    initialColorHex: Long = MEMO_COLORS[0],
    initialIsAllDay: Boolean = true,
    initialStartHour: Int = 8,
    initialStartMin: Int = 0,
    initialEndHour: Int = 9,
    initialEndMin: Int = 0,
    initialReminderMinutes: Int = -1,
    onSave: (title: String, colorHex: Long, startMin: Int, endMin: Int, isAllDay: Boolean, reminderMinutes: Int) -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var selectedColor by remember { mutableLongStateOf(initialColorHex) }
    var isAllDay by remember { mutableStateOf(initialIsAllDay) }
    var startHour by remember { mutableIntStateOf(initialStartHour) }
    var startMin by remember { mutableIntStateOf(initialStartMin) }
    var endHour by remember { mutableIntStateOf(initialEndHour) }
    var endMin by remember { mutableIntStateOf(initialEndMin) }
    var reminderMinutes by remember { mutableIntStateOf(initialReminderMinutes) }

    var isTodo by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showCustomReminderInput by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. 상단 바
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color(0xFF1E1E1E)
                        )
                    }
                    Text(
                        text = "메모",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E1E1E)
                        )
                    )
                    TextButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                val sm = if (isAllDay) -1 else startHour * 60 + startMin
                                val em = if (isAllDay) -1 else endHour * 60 + endMin
                                onSave(title.trim(), selectedColor, sm, em, isAllDay, reminderMinutes)
                            }
                        }
                    ) {
                        Text(
                            "저장",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1976D2)
                            )
                        )
                    }
                }

                // 2. 본문 영역 (광고 배너 영역 제외)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 메모 입력창 (선택된 색상이 입력창 배경에 연하게 반영됨)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(selectedColor).copy(alpha = 0.15f))
                            .border(1.dp, Color(selectedColor).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        BasicTextField(
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp),
                            textStyle = TextStyle(fontSize = 16.sp, color = Color(0xFF1E1E1E)),
                            decorationBox = { innerTextField ->
                                if (title.isEmpty()) {
                                    Text("메모", color = Color(0xFF9E9E9E), fontSize = 16.sp)
                                }
                                innerTextField()
                            }
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // 색상 선택 Row
                    SettingsRow(
                        label = "색",
                        onClick = { showColorPicker = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color(0xFF757575),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(selectedColor))
                                    .border(1.dp, Color.LightGray, CircleShape)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    // 할 일 Row
                    SettingsRow(
                        leadingIcon = {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF757575)
                            )
                        },
                        label = "할 일"
                    ) {
                        Switch(
                            checked = isTodo,
                            onCheckedChange = { isTodo = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF1976D2)
                            )
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // 기간 섹션 타이틀
                    Text(
                        text = "기간",
                        color = Color(0xFF1976D2),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // 하루 종일 Row
                    SettingsRow(label = "하루 종일") {
                        Switch(
                            checked = isAllDay,
                            onCheckedChange = { isAllDay = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF1976D2)
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    // 시작일 Row
                    SettingsRow(
                        label = "시작일",
                        onClick = { showStartPicker = !showStartPicker; showEndPicker = false }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("2026.07.07", fontSize = 15.sp, color = Color(0xFF1E1E1E))
                            if (!isAllDay) {
                                VerticalDivider(modifier = Modifier.height(12.dp), color = Color.LightGray)
                                Text(
                                    text = "%02d:%02d".format(startHour, startMin),
                                    fontSize = 15.sp,
                                    color = Color(0xFF1E1E1E)
                                )
                            }
                        }
                    }

                    if (showStartPicker && !isAllDay) {
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                            com.todaywork.app.ui.components.WheelTimePicker(
                                initialHour = startHour,
                                initialMinute = startMin,
                                onTimeSelected = { h, m -> startHour = h; startMin = m }
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    // 종료일 Row
                    SettingsRow(
                        label = "종료일",
                        onClick = { showEndPicker = !showEndPicker; showStartPicker = false }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("2026.07.07", fontSize = 15.sp, color = Color(0xFF1E1E1E))
                            if (!isAllDay) {
                                VerticalDivider(modifier = Modifier.height(12.dp), color = Color.LightGray)
                                Text(
                                    text = "%02d:%02d".format(endHour, endMin),
                                    fontSize = 15.sp,
                                    color = Color(0xFF1E1E1E)
                                )
                            }
                        }
                    }

                    if (showEndPicker && !isAllDay) {
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                            com.todaywork.app.ui.components.WheelTimePicker(
                                initialHour = endHour,
                                initialMinute = endMin,
                                onTimeSelected = { h, m -> endHour = h; endMin = m }
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    // 알림 Row
                    SettingsRow(
                        leadingIcon = {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color(0xFF757575)
                            )
                        },
                        label = "알림",
                        onClick = { showReminderDialog = true }
                    ) {
                        Text(
                            text = reminderLabel(reminderMinutes),
                            fontSize = 14.sp,
                            color = if (reminderMinutes >= 0) Color(0xFF1976D2) else Color(0xFF9E9E9E)
                        )
                    }

                    // 삭제 버튼 (수정 모드일 때 노출)
                    if (onDelete != null) {
                        Spacer(Modifier.height(24.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDelete(); onDismiss() }
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFE53935)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "삭제",
                                color = Color(0xFFE53935),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // 색상 선택 BottomSheet (m.jpeg 레이아웃 완벽 이식)
    if (showColorPicker) {
        ModalBottomSheet(
            onDismissRequest = { showColorPicker = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "기본 색상",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E1E1E)
                )

                // 19종 색상 그리드 (5열 배치)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val rows = (MEMO_COLORS.size + 4) / 5
                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (c in 0 until 5) {
                                val idx = r * 5 + c
                                if (idx < MEMO_COLORS.size) {
                                    val color = MEMO_COLORS[idx]
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(color))
                                            .then(
                                                if (color == selectedColor) {
                                                    Modifier.border(3.dp, Color(0xFF1976D2), CircleShape)
                                                } else {
                                                    Modifier.border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
                                                }
                                            )
                                            .clickable {
                                                selectedColor = color
                                                showColorPicker = false
                                            }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(46.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 닫기 버튼
                TextButton(
                    onClick = { showColorPicker = false },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 24.dp)
                ) {
                    Text(
                        "닫기",
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    // 알림 옵션 다이얼로그
    if (showReminderDialog) {
        val options = listOf(
            "없음" to -1,
            "일정 시작시간" to 0,
            "5분 전" to 5,
            "10분 전" to 10,
            "30분 전" to 30,
            "1시간 전" to 60,
            "직접 추가" to Int.MIN_VALUE
        )
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text("알림") },
            text = {
                Column {
                    options.forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (value == Int.MIN_VALUE) {
                                        showReminderDialog = false
                                        showCustomReminderInput = true
                                    } else {
                                        reminderMinutes = value
                                        showReminderDialog = false
                                    }
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (reminderMinutes == value) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(label, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showReminderDialog = false }) { Text("취소") }
            }
        )
    }

    // 직접 입력 다이얼로그
    if (showCustomReminderInput) {
        var customText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCustomReminderInput = false },
            title = { Text("직접 입력") },
            text = {
                OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it.filter { c -> c.isDigit() } },
                    label = { Text("분 전") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    customText.toIntOrNull()?.let { reminderMinutes = it }
                    showCustomReminderInput = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomReminderInput = false }) { Text("취소") }
            }
        )
    }
}

private fun reminderLabel(minutes: Int): String = when {
    minutes < 0 -> "없음"
    minutes == 0 -> "일정 시작시간"
    minutes < 60 -> "${minutes}분 전"
    else -> "${minutes / 60}시간 전"
}

// 공통 설정 Row 컴포넌트
@Composable
private fun SettingsRow(
    leadingIcon: @Composable (() -> Unit)? = null,
    label: String,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = label,
            fontSize = 15.sp,
            color = Color(0xFF1E1E1E),
            modifier = Modifier.weight(1f)
        )
        content()
    }
}

// ── 근무 수정 다이얼로그 ──────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkEditDialog(
    dayInfo: DayInfo,
    onSave: (shiftName: String, sh: Int, sm: Int, eh: Int, em: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(dayInfo.shiftType ?: ShiftType.DAY) }
    var startHour by remember { mutableIntStateOf(selectedType.defaultStartHour) }
    var startMin by remember { mutableIntStateOf(selectedType.defaultStartMin) }
    var endHour by remember { mutableIntStateOf(selectedType.defaultEndHour) }
    var endMin by remember { mutableIntStateOf(selectedType.defaultEndMin) }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${dayInfo.date.monthValue}월 ${dayInfo.date.dayOfMonth}일 근무 수정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "${selectedType.shortLabel} ${selectedType.label}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("근무 타입") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        ShiftType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(Modifier.size(12.dp).clip(CircleShape).background(type.toColor()))
                                        Text(type.label)
                                    }
                                },
                                onClick = {
                                    selectedType = type
                                    startHour = type.defaultStartHour
                                    startMin = type.defaultStartMin
                                    endHour = type.defaultEndHour
                                    endMin = type.defaultEndMin
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedType.isWorkDay) {
                    Text("시작 시간 설정",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    com.todaywork.app.ui.components.WheelTimePicker(
                        initialHour = startHour,
                        initialMinute = startMin,
                        onTimeSelected = { h, m -> startHour = h; startMin = m },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("종료 시간 설정",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    com.todaywork.app.ui.components.WheelTimePicker(
                        initialHour = endHour,
                        initialMinute = endMin,
                        onTimeSelected = { h, m -> endHour = h; endMin = m },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedType.name, startHour, startMin, endHour, endMin) }) {
                Text("저장")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

// ── 패턴 구간 적용 다이얼로그 ───────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplyPatternDialog(
    patterns: List<com.todaywork.app.data.db.entity.ShiftPatternEntity>,
    onApply: (patternId: Long, startDate: LocalDate, endDate: LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPattern by remember { mutableStateOf(patterns.firstOrNull()) }
    var patternExpanded by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    var startDate by remember { mutableStateOf(today) }
    var endDate by remember { mutableStateOf(today.plusMonths(1)) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("달력에 근무 패턴 추가") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (patterns.isEmpty()) {
                    Text(
                        "먼저 설정 탭에서 근무 패턴을 생성해주세요.",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = patternExpanded,
                        onExpandedChange = { patternExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedPattern?.name ?: "패턴 선택",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("적용할 패턴") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(patternExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = patternExpanded,
                            onDismissRequest = { patternExpanded = false }
                        ) {
                            patterns.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.name) },
                                    onClick = { selectedPattern = p; patternExpanded = false }
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text("적용 구간 설정", style = MaterialTheme.typography.titleSmall)

                    OutlinedButton(
                        onClick = { showStartPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("시작일: ${startDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))}")
                    }

                    OutlinedButton(
                        onClick = { showEndPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("종료일: ${endDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))}")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pId = selectedPattern?.id ?: return@Button
                    onApply(pId, startDate, endDate)
                },
                enabled = selectedPattern != null && !startDate.isAfter(endDate)
            ) {
                Text("적용")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )

    if (showStartPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        startDate = java.time.Instant.ofEpochMilli(ms)
                            .atZone(java.time.ZoneId.of("UTC")).toLocalDate()
                    }
                    showStartPicker = false
                }) { Text("선택") }
            },
            dismissButton = { TextButton(onClick = { showStartPicker = false }) { Text("취소") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        endDate = java.time.Instant.ofEpochMilli(ms)
                            .atZone(java.time.ZoneId.of("UTC")).toLocalDate()
                    }
                    showEndPicker = false
                }) { Text("선택") }
            },
            dismissButton = { TextButton(onClick = { showEndPicker = false }) { Text("취소") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
