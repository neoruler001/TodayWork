package com.todaywork.app.ui.screens.schedule

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.hilt.navigation.compose.hiltViewModel
import com.todaywork.app.data.db.entity.ShiftPatternEntity
import com.todaywork.app.data.model.ShiftType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::showAddDialog,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("패턴 추가") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── 타이틀 ──────────────────────────────────────────
            Text(
                text     = "근무 패턴 관리",
                style    = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            // ── 현재 활성 패턴 강조 ──────────────────────────────
            val activePattern = uiState.patterns.find { it.id == uiState.activePatternId }
            if (activePattern != null) {
                ActivePatternCard(
                    pattern = activePattern,
                    cycles  = viewModel.parseCycles(activePattern),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            } else {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text     = "⚠️ 활성화된 근무 패턴이 없습니다. 패턴을 추가하고 활성화하세요.",
                        modifier = Modifier.padding(16.dp),
                        style    = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── 패턴 목록 ──────────────────────────────────────
            Text(
                text     = "저장된 패턴 (${uiState.patterns.size})",
                style    = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.patterns, key = { it.id }) { pattern ->
                    PatternCard(
                        pattern   = pattern,
                        cycles    = viewModel.parseCycles(pattern),
                        isActive  = pattern.id == uiState.activePatternId,
                        onActivate = { viewModel.activatePattern(pattern.id) },
                        onDelete   = { viewModel.deletePattern(pattern.id) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // ── 패턴 추가 다이얼로그 ─────────────────────────────────────
    if (uiState.showAddDialog) {
        AddPatternDialog(
            presets   = viewModel.presets,
            onConfirm = { name, cycles, startDate, offset ->
                viewModel.addPattern(name, cycles, startDate, offset)
            },
            onDismiss = viewModel::hideAddDialog
        )
    }

    // ── 에러 스낵바 ──────────────────────────────────────────────
    uiState.errorMessage?.let { msg ->
        LaunchedEffect(msg) {
            viewModel.clearError()
        }
    }
}

// ── 현재 활성 패턴 카드 ───────────────────────────────────────
@Composable
private fun ActivePatternCard(
    pattern: ShiftPatternEntity,
    cycles: List<ShiftType>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text  = "현재 근무 패턴",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text  = pattern.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            ShiftCycleRow(cycles = cycles)
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "시작일: ${LocalDate.ofEpochDay(pattern.startDateEpoch).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// ── 패턴 카드 ────────────────────────────────────────────────
@Composable
private fun PatternCard(
    pattern: ShiftPatternEntity,
    cycles: List<ShiftType>,
    isActive: Boolean,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = if (isActive)
                Color(0xFFFFFDE7)
            else MaterialTheme.colorScheme.surface
        ),
        border   = if (isActive)
            BorderStroke(1.dp, Color(0xFFFFEE58))
        else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text  = pattern.name,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                if (!isActive) {
                    TextButton(onClick = onActivate) { Text("활성화", fontSize = 12.sp) }
                } else {
                    Text(
                        text  = "✓ 활성",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, "삭제",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(6.dp))
            ShiftCycleRow(cycles = cycles)
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "${cycles.size}일 사이클 · 시작 ${LocalDate.ofEpochDay(pattern.startDateEpoch).format(DateTimeFormatter.ofPattern("yy.MM.dd"))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("패턴 삭제") },
            text  = { Text("'${pattern.name}' 패턴을 삭제하면 이 패턴으로 생성된 근무 기록도 삭제됩니다.") },
            confirmButton = {
                Button(
                    onClick = { onDelete(); showDeleteConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("삭제") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("취소") }
            }
        )
    }
}

// ── 사이클 Row ───────────────────────────────────────────────
@Composable
fun ShiftCycleRow(cycles: List<ShiftType>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        cycles.forEach { type ->
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(type.toColor()),
                contentAlignment = Alignment.Center
            ) {
                Text(type.shortLabel, fontSize = 10.sp, color = type.badgeTextColor(), fontWeight = FontWeight.Bold)
            }
        }
        Text(
            text  = "(${cycles.size}일)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// ── 패턴 추가 다이얼로그 ──────────────────────────────────────
@Composable
private fun AddPatternDialog(
    presets: List<Pair<String, List<ShiftType>>>,
    onConfirm: (name: String, cycles: List<ShiftType>, startDate: LocalDate, offset: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var patternName  by remember { mutableStateOf("") }
    var cycles       by remember { mutableStateOf<List<ShiftType>>(emptyList()) }
    var startDate    by remember { mutableStateOf(LocalDate.now()) }
    var offsetDay    by remember { mutableIntStateOf(0) }
    var showPresets  by remember { mutableStateOf(true) }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("근무 패턴 추가") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // 패턴 이름
                OutlinedTextField(
                    value = patternName,
                    onValueChange = { patternName = it },
                    label = { Text("패턴 이름") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 프리셋
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("빠른 선택:", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { showPresets = !showPresets }) {
                        Text(if (showPresets) "접기" else "펼치기", fontSize = 12.sp)
                    }
                }
                if (showPresets) {
                    presets.forEach { (name, preset) ->
                        OutlinedButton(
                            onClick = {
                                patternName = name
                                cycles = preset
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(0.dp)
                        ) {
                            Text(name, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            ShiftCycleRow(cycles = preset)
                        }
                    }
                }

                // 현재 사이클 표시
                if (cycles.isNotEmpty()) {
                    Text("사이클 구성:", style = MaterialTheme.typography.bodySmall)
                    ShiftCycleRow(cycles = cycles)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ShiftType.entries.take(5).forEach { type ->
                            OutlinedButton(
                                onClick = { cycles = cycles + type },
                                contentPadding = PaddingValues(4.dp),
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(type.shortLabel, fontSize = 10.sp)
                            }
                        }
                        TextButton(onClick = { if (cycles.isNotEmpty()) cycles = cycles.dropLast(1) }) {
                            Text("↩", fontSize = 16.sp)
                        }
                    }
                } else {
                    Text("근무 타입을 눌러 사이클을 구성하세요:", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ShiftType.entries.forEach { type ->
                            OutlinedButton(
                                onClick = { cycles = cycles + type },
                                contentPadding = PaddingValues(4.dp),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Text(type.shortLabel, fontSize = 9.sp)
                            }
                        }
                    }
                }

                // 시작일
                var showDatePicker by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("시작일: ${startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}")
                }

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    )
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { ms ->
                                    startDate = java.time.Instant.ofEpochMilli(ms).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                }
                                showDatePicker = false
                            }) { Text("선택") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("취소") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                // 오프셋
                OutlinedTextField(
                    value = offsetDay.toString(),
                    onValueChange = { raw -> raw.toIntOrNull()?.let { offsetDay = it } },
                    label = { Text("사이클 오프셋 (0부터 시작)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(patternName, cycles, startDate, offsetDay)
            }) { Text("추가") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}
