package com.todaywork.app.ui.screens.calendar

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import com.todaywork.app.ui.components.ShiftBadgeChip
import com.todaywork.app.ui.components.ShiftLegend
import com.todaywork.app.ui.theme.LunarText
import com.todaywork.app.ui.theme.WeekendSat
import com.todaywork.app.ui.theme.WeekendSun
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showApplyPatternDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── 월 네비게이션 헤더 ──────────────────────────────────
        CalendarHeader(
            year  = uiState.year,
            month = uiState.month,
            onPrev  = viewModel::goToPreviousMonth,
            onNext  = viewModel::goToNextMonth,
            onToday = viewModel::goToToday
        )

        // ── 요일 헤더 ──────────────────────────────────────────
        WeekDayHeader()

        // ── 달력 그리드 ────────────────────────────────────────
        if (uiState.isLoading) {
            Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            CalendarGrid(
                year         = uiState.year,
                month        = uiState.month,
                days         = uiState.days,
                selectedDate = uiState.selectedDate,
                showLunar    = uiState.showLunar,
                showHoliday  = uiState.showHoliday,
                onDateClick  = viewModel::selectDate
            )
        }

        // ── 범례 및 기능 버튼 ────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ShiftLegend(modifier = Modifier.weight(1f))
            Button(
                onClick = { showApplyPatternDialog = true },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("근무 패턴 추가", fontSize = 12.sp)
            }
        }

        HorizontalDivider()

        // ── 선택된 날짜 상세 ────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.selectedDate != null,
            enter   = fadeIn() + slideInVertically { it / 2 },
            exit    = fadeOut() + slideOutVertically { it / 2 }
        ) {
            val dayInfo = viewModel.getSelectedDayInfo()
            if (dayInfo != null) {
                DayDetailPanel(
                    dayInfo      = dayInfo,
                    onEdit       = { showEditDialog = true },
                    onReset      = { viewModel.resetDateToPattern(dayInfo.date) },
                    onDismiss    = viewModel::clearSelection
                )
            }
        }
    }

    // ── 근무 수정 다이얼로그 ─────────────────────────────────────
    if (showEditDialog) {
        val dayInfo = viewModel.getSelectedDayInfo()
        if (dayInfo != null) {
            WorkEditDialog(
                dayInfo = dayInfo,
                onSave  = { shiftName, sh, sm, eh, em, memo ->
                    viewModel.saveWorkRecord(dayInfo.date, shiftName, sh, sm, eh, em, memo)
                    showEditDialog = false
                },
                onDismiss = { showEditDialog = false }
            )
        }
    }

    // ── 패턴 구간 적용 다이얼로그 ────────────────────────────────
    if (showApplyPatternDialog) {
        ApplyPatternDialog(
            patterns  = uiState.patterns,
            onApply   = { patternId, startDate, endDate ->
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
    onPrev: () -> Unit, onNext: () -> Unit, onToday: () -> Unit
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
            text       = "${year}년 ${month}월",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.weight(1f),
            textAlign  = TextAlign.Center,
            color      = MaterialTheme.colorScheme.onBackground
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
                text       = d,
                modifier   = Modifier.weight(1f),
                textAlign  = TextAlign.Center,
                style      = MaterialTheme.typography.labelMedium,
                color      = when (idx) {
                    0    -> WeekendSun
                    6    -> WeekendSat
                    else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

// ── 달력 그리드 ────────────────────────────────────────────────
@Composable
private fun CalendarGrid(
    year: Int, month: Int,
    days: List<DayInfo>,
    selectedDate: LocalDate?,
    showLunar: Boolean,
    showHoliday: Boolean,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDay  = LocalDate.of(year, month, 1)
    val startPad  = firstDay.dayOfWeek.value % 7  // 일=0, 월=1, ..., 토=6
    val totalDays = firstDay.lengthOfMonth()
    val today     = LocalDate.now()

    LazyVerticalGrid(
        columns       = GridCells.Fixed(7),
        modifier      = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        contentPadding = PaddingValues(bottom = 4.dp)
    ) {
        // 앞쪽 빈 셀
        items(startPad) { Box(Modifier.aspectRatio(0.85f)) }

        // 날짜 셀
        items(totalDays) { index ->
            val dayInfo  = days.getOrNull(index)
            val date     = firstDay.plusDays(index.toLong())
            val isToday  = date == today
            val isSelected = date == selectedDate
            val dow      = date.dayOfWeek

            CalendarCell(
                dayInfo    = dayInfo,
                date       = date,
                isToday    = isToday,
                isSelected = isSelected,
                showLunar  = showLunar,
                showHoliday = showHoliday,
                dow        = dow,
                onClick    = { onDateClick(date) }
            )
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
    onClick: () -> Unit
) {
    val shift     = dayInfo?.shiftType
    val isHoliday = dayInfo?.isHoliday == true
    val hasMemo   = dayInfo?.memo?.isNotBlank() == true

    val normalDateColor = when {
        isHoliday || dow == DayOfWeek.SUNDAY -> WeekendSun
        dow == DayOfWeek.SATURDAY            -> WeekendSat
        else                                 -> MaterialTheme.colorScheme.onBackground
    }

    Box(
        modifier = Modifier
            .aspectRatio(0.9f)
            .padding(1.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else Color.Transparent
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 3.dp, bottom = 3.dp)
        ) {
            // 날짜 숫자 — 오늘은 채워진 원형 배경
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
                    text       = date.dayOfMonth.toString(),
                    fontSize   = 12.sp,
                    fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
                    color      = if (isToday) Color.White else normalDateColor
                )
            }

            Spacer(Modifier.height(2.dp))

            // 근무 배지 — 전체 너비 pill
            if (shift != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shift.toColor()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = shift.shortLabel,
                        fontSize   = 9.sp,
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(vertical = 1.dp)
                    )
                }
            } else {
                Spacer(Modifier.height(13.dp))
            }

            // 음력
            if (showLunar && dayInfo?.lunarDay?.isNotBlank() == true) {
                Text(
                    text     = dayInfo.lunarDay,
                    fontSize = 7.sp,
                    color    = LunarText,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 1.dp)
                )
            } else {
                Spacer(Modifier.height(9.dp))
            }

            // 점 인디케이터: 메모(보라) + 공휴일(빨강, 일요일 제외)
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (hasMemo) {
                    Box(
                        Modifier.size(4.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                }
                if (showHoliday && isHoliday && dow != DayOfWeek.SUNDAY) {
                    Box(
                        Modifier.size(4.dp).clip(CircleShape)
                            .background(WeekendSun)
                    )
                }
            }
        }
    }
}

// ── 날짜 상세 패널 ────────────────────────────────────────────
@Composable
private fun DayDetailPanel(
    dayInfo: DayInfo,
    onEdit: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val shift = dayInfo.shiftType

    Surface(
        modifier      = Modifier.fillMaxWidth(),
        color         = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text  = "${dayInfo.date.monthValue}월 ${dayInfo.date.dayOfMonth}일",
                    style = MaterialTheme.typography.titleMedium
                )
                if (dayInfo.isHoliday) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text   = dayInfo.holidayName,
                        style  = MaterialTheme.typography.bodySmall,
                        color  = WeekendSun
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, "닫기", modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (shift != null) {
                    ShiftBadgeChip(shiftType = shift)
                    if (dayInfo.startTime.isNotBlank()) {
                        Text(
                            text  = "${dayInfo.startTime} ~ ${dayInfo.endTime}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    Text("근무 없음", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                if (dayInfo.isModified) {
                    Text("(수정됨)", fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.tertiary)
                }
            }

            if (dayInfo.lunarDay.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("음력 ${dayInfo.lunarDay}",
                    style = MaterialTheme.typography.bodySmall, color = LunarText)
            }

            if (dayInfo.memo.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    dayInfo.memo.split("\n").filter { it.isNotBlank() }.forEach { line ->
                        Text(
                            text  = line,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("수정")
                }
                if (dayInfo.isModified) {
                    OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("패턴으로 복원")
                    }
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
    var startMin  by remember { mutableIntStateOf(selectedType.defaultStartMin) }
    var endHour   by remember { mutableIntStateOf(selectedType.defaultEndHour) }
    var endMin    by remember { mutableIntStateOf(selectedType.defaultEndMin) }
    var memo      by remember { mutableStateOf(dayInfo.memo) }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("${dayInfo.date.monthValue}월 ${dayInfo.date.dayOfMonth}일 근무 수정")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 근무 타입 선택
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
                                            Modifier.size(12.dp).clip(CircleShape)
                                                .background(type.toColor())
                                        )
                                        Text(type.label)
                                    }
                                },
                                onClick = {
                                    selectedType = type
                                    startHour = type.defaultStartHour
                                    startMin  = type.defaultStartMin
                                    endHour   = type.defaultEndHour
                                    endMin    = type.defaultEndMin
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // 시간 입력 (근무일인 경우만)
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

                // 메모
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
                    Text("먼저 설정 탭에서 근무 패턴을 생성해주세요.", color = MaterialTheme.colorScheme.error)
                } else {
                    // 패턴 선택
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
                    val sDate = runCatching { LocalDate.of(startYear.toInt(), startMonth.toInt(), startDay.toInt()) }.getOrNull()
                    val eDate = runCatching { LocalDate.of(endYear.toInt(), endMonth.toInt(), endDay.toInt()) }.getOrNull()
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
