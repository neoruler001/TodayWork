package com.todaywork.app.ui.screens.schedule

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPatternDialog(
    presets: List<Pair<String, List<ShiftType>>>,
    onConfirm: (name: String, cycles: List<ShiftType>, startDate: LocalDate, offset: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val defaultCycles = listOf(
        ShiftType.DAY, ShiftType.DAY, ShiftType.REST, ShiftType.REST,
        ShiftType.NIGHT, ShiftType.NIGHT, ShiftType.REST, ShiftType.REST
    )
    var patternName by remember { mutableStateOf("주주휴휴야야휴휴") }
    var cycles      by remember { mutableStateOf<List<ShiftType>>(defaultCycles) }
    var startDate   by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 상단 바
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
                    }
                    Text(
                        text = "근무 패턴 추가",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = { if (patternName.isNotBlank() && cycles.isNotEmpty()) onConfirm(patternName, cycles, startDate, 0) },
                        enabled = patternName.isNotBlank() && cycles.isNotEmpty()
                    ) {
                        Text("저장", fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 패턴 이름
                    OutlinedTextField(
                        value = patternName,
                        onValueChange = { patternName = it },
                        label = { Text("패턴 이름") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 현재 구성 카드
                    Text(
                        "현재 구성",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (cycles.isEmpty()) {
                                Text(
                                    "근무 타입을 추가하세요",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            } else {
                                ShiftCycleRow(cycles = cycles)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${cycles.size}일 사이클",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    // 근무 추가 버튼
                    Text(
                        "근무 추가",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            ShiftType.entries.take(6).forEach { type ->
                                OutlinedButton(
                                    onClick = { cycles = cycles + type },
                                    contentPadding = PaddingValues(4.dp),
                                    modifier = Modifier.size(40.dp),
                                    shape = RoundedCornerShape(4.dp)
                                ) { Text(type.shortLabel, fontSize = 10.sp) }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            ShiftType.entries.drop(6).forEach { type ->
                                OutlinedButton(
                                    onClick = { cycles = cycles + type },
                                    contentPadding = PaddingValues(4.dp),
                                    modifier = Modifier.size(40.dp),
                                    shape = RoundedCornerShape(4.dp)
                                ) { Text(type.shortLabel, fontSize = 10.sp) }
                            }
                            OutlinedButton(
                                onClick = { if (cycles.isNotEmpty()) cycles = cycles.dropLast(1) },
                                contentPadding = PaddingValues(4.dp),
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) { Text("↩", fontSize = 14.sp) }
                            OutlinedButton(
                                onClick = { cycles = emptyList() },
                                contentPadding = PaddingValues(4.dp),
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) { Text("✕", fontSize = 12.sp) }
                        }
                    }

                    // 빠른 선택
                    Text(
                        "빠른 선택",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        presets.forEach { (name, preset) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { patternName = name; cycles = preset },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ShiftCycleRow(cycles = preset)
                                }
                            }
                        }
                    }

                    // 시작일
                    Text(
                        "시작일",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
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
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("취소") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
