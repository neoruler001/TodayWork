package com.todaywork.app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.todaywork.app.data.model.ShiftType
import com.todaywork.app.ui.theme.LunarText
import com.todaywork.app.ui.theme.WeekendSat
import com.todaywork.app.ui.theme.WeekendSun
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showApplyPatternDialog by remember { mutableStateOf(false) }
    var showDaySheet by remember { mutableStateOf(false) }

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
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }

    if (showDaySheet) {
        val dayInfo = viewModel.getSelectedDayInfo()
        if (dayInfo != null) {
            DayDetailBottomSheet(
                dayInfo = dayInfo,
                onEdit = { showEditDialog = true },
                onReset = { viewModel.resetDateToPattern(dayInfo.date) },
                onDismiss = {
                    showDaySheet = false
                    viewModel.clearSelection()
                }
            )
        } else {
            showDaySheet = false
        }
    }

    if (showEditDialog) {
        val dayInfo = viewModel.getSelectedDayInfo()
        if (dayInfo != null) {
            WorkEditDialog(
                dayInfo = dayInfo,
                onSave = { shiftName, sh, sm, eh, em, memo ->
                    viewModel.saveWorkRecord(dayInfo.date, shiftName, sh, sm, eh, em, memo)
                    showEditDialog = false
                },
                onDismiss = { showEditDialog = false }
            )
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
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft, "이전 달",
                tint = MaterialTheme.colorScheme.onBackground
            )
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
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, "다음 달",
                tint = MaterialTheme.colorScheme.onBackground
            )
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

// ── 달력 그리드 (비스크롤, fillMaxSize) ────────────────────────
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
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
    val hasMemo = dayInfo?.memo?.isNotBlank() == true
    val memoLines = dayInfo?.memo?.lines()?.filter { it.isNotBlank() } ?: emptyList()
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
                        Box(
                            Modifier.size(4.dp).clip(CircleShape).background(WeekendSun)
                        )
                    }
                    if (hasMemo) {
                        Box(
                            Modifier.size(4.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary)
                        )
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

            // 메모 라인 (최대 2줄)
            memoLines.take(2).forEach { line ->
                Text(
                    text = line,
                    fontSize = 7.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                        .padding(horizontal = 2.dp)
                )
            }
        }
    }
}

// ── 날짜 상세 바텀시트 ────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDetailBottomSheet(
    dayInfo: DayInfo,
    onEdit: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val shift = dayInfo.shiftType
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 날짜 + 공휴일명
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${dayInfo.date.year}년 ${dayInfo.date.monthValue}월 ${dayInfo.date.dayOfMonth}일",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (dayInfo.isHoliday) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = dayInfo.holidayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WeekendSun
                    )
                }
            }

            HorizontalDivider()

            // 근무 배지 + 정보
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (shift != null) {
                    if (shift.isWorkDay) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(shift.toColor()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = shift.shortLabel,
                                fontSize = 24.sp,
                                color = shift.badgeTextColor(),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.size(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = shift.shortLabel,
                                fontSize = 30.sp,
                                color = shift.toColor(),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = shift.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (dayInfo.isModified) {
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "(수정됨)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                        if (dayInfo.startTime.isNotBlank()) {
                            Text(
                                text = "${dayInfo.startTime} ~ ${dayInfo.endTime}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    Text(
                        "근무 없음",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // 음력
            if (dayInfo.lunarDay.isNotBlank()) {
                Text(
                    "음력 ${dayInfo.lunarDay}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LunarText
                )
            }

            // 메모
            if (dayInfo.memo.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "메모",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(dayInfo.memo, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // 버튼
            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("근무 수정")
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
}

// ── 근무 수정 다이얼로그 ──────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkEditDialog(
    dayInfo: DayInfo,
    onSave: (shiftName: String, sh: Int, sm: Int, eh: Int, em: Int, memo: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(dayInfo.shiftType ?: ShiftType.DAY) }
    var startHour by remember { mutableIntStateOf(selectedType.defaultStartHour) }
    var startMin by remember { mutableIntStateOf(selectedType.defaultStartMin) }
    var endHour by remember { mutableIntStateOf(selectedType.defaultEndHour) }
    var endMin by remember { mutableIntStateOf(selectedType.defaultEndMin) }
    var memo by remember { mutableStateOf(dayInfo.memo) }
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
                                            Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(type.toColor())
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

                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text("메모") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    singleLine = false
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(selectedType.name, startHour, startMin, endHour, endMin, memo)
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
                                    onClick = {
                                        selectedPattern = p
                                        patternExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text("시작일 (YYYY-MM-DD)", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startYear,
                            onValueChange = { startYear = it },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = startMonth,
                            onValueChange = { startMonth = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = startDay,
                            onValueChange = { startDay = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Text("종료일 (YYYY-MM-DD)", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = endYear,
                            onValueChange = { endYear = it },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = endMonth,
                            onValueChange = { endMonth = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = endDay,
                            onValueChange = { endDay = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
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
