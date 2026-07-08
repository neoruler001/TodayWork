package com.todaywork.app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import java.time.format.TextStyle
import java.util.Locale

private val MEMO_COLORS = listOf(
    0xFF26C6DAL, // teal
    0xFF7986CBL, // indigo
    0xFF66BB6AL, // green
    0xFFEF5350L, // red
    0xFFFFA726L, // orange
    0xFFAB47BCL, // purple
    0xFF78909CL, // blue-grey
    0xFFEC407AL, // pink
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDaySheet by remember { mutableStateOf(false) }
    var showApplyPatternDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.year, uiState.month) {
        showDaySheet = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CalendarHeader(
            year = uiState.year,
            month = uiState.month,
            onPrev = viewModel::goToPreviousMonth,
            onNext = viewModel::goToNextMonth,
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
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        }
    }

    if (showDaySheet) {
        val dayInfo = viewModel.getSelectedDayInfo()
        if (dayInfo != null) {
            DayDetailBottomSheet(
                dayInfo = dayInfo,
                onSaveShift = { shiftName, sh, sm, eh, em ->
                    viewModel.saveWorkRecord(dayInfo.date, shiftName, sh, sm, eh, em)
                },
                onResetShift = { viewModel.resetDateToPattern(dayInfo.date) },
                onAddMemo = { title, colorHex, startMin, endMin, isAllDay ->
                    viewModel.addMemo(dayInfo.date, title, colorHex, startMin, endMin, isAllDay)
                },
                onDeleteMemo = viewModel::deleteMemo,
                onDismiss = {
                    showDaySheet = false
                    viewModel.clearSelection()
                }
            )
        } else {
            showDaySheet = false
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
            // 날짜 번호 + 인디케이터 점
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

            // 음력
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

            // 근무 배지
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

            // 공휴일명
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

            // 메모 바 (최대 2개, 3개 이상이면 마지막 옆 "more...")
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
                        fontSize = 7.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ── 날짜 상세 바텀시트 (3탭) ──────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDetailBottomSheet(
    dayInfo: DayInfo,
    onSaveShift: (shiftName: String, sh: Int, sm: Int, eh: Int, em: Int) -> Unit,
    onResetShift: () -> Unit,
    onAddMemo: (title: String, colorHex: Long, startMin: Int, endMin: Int, isAllDay: Boolean) -> Unit,
    onDeleteMemo: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }
    var showWorkEditDialog by remember { mutableStateOf(false) }
    var showMemoAddDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 날짜 헤더
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 4.dp, bottom = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val dayOfWeekName = dayInfo.date.dayOfWeek
                        .getDisplayName(TextStyle.SHORT, Locale.KOREAN)
                    Text(
                        text = "${dayInfo.date.monthValue}월 ${dayInfo.date.dayOfMonth}일 ${dayOfWeekName}요일",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = when (dayInfo.date.dayOfWeek) {
                            DayOfWeek.SUNDAY -> if (dayInfo.isHoliday) WeekendSun else WeekendSun
                            DayOfWeek.SATURDAY -> WeekendSat
                            else -> if (dayInfo.isHoliday) WeekendSun else MaterialTheme.colorScheme.onBackground
                        }
                    )
                    if (dayInfo.lunarDay.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "음 ${dayInfo.lunarDay}",
                            style = MaterialTheme.typography.bodySmall,
                            color = LunarText
                        )
                    }
                }
                if (dayInfo.isHoliday) {
                    Text(
                        dayInfo.holidayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = WeekendSun
                    )
                }

                Spacer(Modifier.height(10.dp))

                // 근무 배지 + 시간
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val shift = dayInfo.shiftType
                    if (shift != null) {
                        if (shift.isWorkDay) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(shift.toColor()),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    shift.shortLabel,
                                    fontSize = 20.sp,
                                    color = shift.badgeTextColor(),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    shift.shortLabel,
                                    fontSize = 26.sp,
                                    color = shift.toColor(),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Column {
                            Text(shift.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            if (dayInfo.startTime.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Schedule, null,
                                        modifier = Modifier.size(13.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Spacer(Modifier.width(3.dp))
                                    Text(
                                        "${dayInfo.startTime} - ${dayInfo.endTime}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            if (dayInfo.isModified) {
                                Text("(수정됨)", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    } else {
                        Text(
                            "근무 없음",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    }
                }
            }

            HorizontalDivider()

            // 탭
            TabRow(selectedTabIndex = selectedTab) {
                listOf("메모", "근무", "급여").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.labelLarge) }
                    )
                }
            }

            // 탭 콘텐츠
            when (selectedTab) {
                0 -> MemoTabContent(
                    memos = dayInfo.memos,
                    onAddClick = { showMemoAddDialog = true },
                    onDelete = onDeleteMemo
                )
                1 -> WorkTabContent(
                    dayInfo = dayInfo,
                    onEditClick = { showWorkEditDialog = true },
                    onReset = onResetShift
                )
                2 -> SalaryTabContent()
            }
        }
    }

    if (showWorkEditDialog) {
        WorkEditDialog(
            dayInfo = dayInfo,
            onSave = { shiftName, sh, sm, eh, em ->
                onSaveShift(shiftName, sh, sm, eh, em)
                showWorkEditDialog = false
            },
            onDismiss = { showWorkEditDialog = false }
        )
    }

    if (showMemoAddDialog) {
        MemoEditDialog(
            onSave = { title, colorHex, startMin, endMin, isAllDay ->
                onAddMemo(title, colorHex, startMin, endMin, isAllDay)
                showMemoAddDialog = false
            },
            onDismiss = { showMemoAddDialog = false }
        )
    }
}

// ── 메모 탭 ───────────────────────────────────────────────────
@Composable
private fun MemoTabContent(
    memos: List<MemoItem>,
    onAddClick: () -> Unit,
    onDelete: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp, max = 380.dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(memos) { memo ->
            MemoCard(memo = memo, onDelete = { onDelete(memo.id) })
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
private fun MemoCard(memo: MemoItem, onDelete: () -> Unit) {
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
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Text(
                text = memo.title,
                fontSize = 15.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.MoreVert, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("삭제") },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
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
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
private fun SalaryTabContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "급여 정보 준비 중",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

// ── 메모 추가 다이얼로그 ──────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemoEditDialog(
    initialTitle: String = "",
    initialColorHex: Long = MEMO_COLORS[0],
    onSave: (title: String, colorHex: Long, startMin: Int, endMin: Int, isAllDay: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var selectedColor by remember { mutableLongStateOf(initialColorHex) }
    var isAllDay by remember { mutableStateOf(true) }
    var startHour by remember { mutableIntStateOf(8) }
    var startMin by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(9) }
    var endMin by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("새 메모") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 제목 (선택된 색상 배경)
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("메모 제목", color = Color.White.copy(alpha = 0.6f)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(selectedColor),
                        unfocusedContainerColor = Color(selectedColor),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                )

                // 색상 선택
                Text("색상", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MEMO_COLORS.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .then(
                                    if (color == selectedColor)
                                        Modifier.border(2.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }

                // 하루 종일 토글
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("하루 종일", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isAllDay, onCheckedChange = { isAllDay = it })
                }

                // 시간 입력 (하루종일 아닌 경우)
                if (!isAllDay) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TimeInputField(
                            label = "시작",
                            hour = startHour,
                            minute = startMin,
                            onHourChange = { startHour = it },
                            onMinuteChange = { startMin = it },
                            modifier = Modifier.weight(1f)
                        )
                        TimeInputField(
                            label = "종료",
                            hour = endHour,
                            minute = endMin,
                            onHourChange = { endHour = it },
                            onMinuteChange = { endMin = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val sm = if (isAllDay) -1 else startHour * 60 + startMin
                        val em = if (isAllDay) -1 else endHour * 60 + endMin
                        onSave(title.trim(), selectedColor, sm, em, isAllDay)
                    }
                }
            ) { Text("저장") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
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
        title = {
            Text("${dayInfo.date.monthValue}월 ${dayInfo.date.dayOfMonth}일 근무 수정")
        },
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
                                        Box(
                                            Modifier.size(12.dp).clip(CircleShape).background(type.toColor())
                                        )
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TimeInputField(
                            label = "시작",
                            hour = startHour,
                            minute = startMin,
                            onHourChange = { startHour = it },
                            onMinuteChange = { startMin = it },
                            modifier = Modifier.weight(1f)
                        )
                        TimeInputField(
                            label = "종료",
                            hour = endHour,
                            minute = endMin,
                            onHourChange = { endHour = it },
                            onMinuteChange = { endMin = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(selectedType.name, startHour, startMin, endHour, endMin)
            }) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@Composable
private fun TimeInputField(
    label: String,
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = "%02d:%02d".format(hour, minute),
        onValueChange = { raw ->
            val digits = raw.filter { it.isDigit() }
            if (digits.length >= 4) {
                val h = digits.substring(0, 2).toIntOrNull() ?: 0
                val m = digits.substring(2, 4).toIntOrNull() ?: 0
                if (h in 0..23) onHourChange(h)
                if (m in 0..59) onMinuteChange(m)
            }
        },
        label = { Text(label) },
        modifier = modifier,
        singleLine = true
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
    var startYear by remember { mutableStateOf(today.year.toString()) }
    var startMonth by remember { mutableStateOf(today.monthValue.toString().padStart(2, '0')) }
    var startDay by remember { mutableStateOf(today.dayOfMonth.toString().padStart(2, '0')) }

    val nextMonth = today.plusMonths(1)
    var endYear by remember { mutableStateOf(nextMonth.year.toString()) }
    var endMonth by remember { mutableStateOf(nextMonth.monthValue.toString().padStart(2, '0')) }
    var endDay by remember { mutableStateOf(nextMonth.dayOfMonth.toString().padStart(2, '0')) }

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

                    Text("시작일 (YYYY-MM-DD)", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = startYear, onValueChange = { startYear = it }, modifier = Modifier.weight(1.5f), singleLine = true)
                        OutlinedTextField(value = startMonth, onValueChange = { startMonth = it }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = startDay, onValueChange = { startDay = it }, modifier = Modifier.weight(1f), singleLine = true)
                    }

                    Text("종료일 (YYYY-MM-DD)", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = endYear, onValueChange = { endYear = it }, modifier = Modifier.weight(1.5f), singleLine = true)
                        OutlinedTextField(value = endMonth, onValueChange = { endMonth = it }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = endDay, onValueChange = { endDay = it }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pId = selectedPattern?.id ?: return@Button
                    val sDate = runCatching {
                        LocalDate.of(startYear.toInt(), startMonth.toInt(), startDay.toInt())
                    }.getOrNull()
                    val eDate = runCatching {
                        LocalDate.of(endYear.toInt(), endMonth.toInt(), endDay.toInt())
                    }.getOrNull()
                    if (sDate != null && eDate != null && !sDate.isAfter(eDate)) {
                        onApply(pId, sDate, eDate)
                    }
                },
                enabled = selectedPattern != null
            ) {
                Text("적용")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}
