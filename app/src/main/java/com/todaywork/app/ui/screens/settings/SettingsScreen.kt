package com.todaywork.app.ui.screens.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.todaywork.app.data.db.entity.AlarmSettingEntity
import com.todaywork.app.data.model.ShiftType
import java.time.LocalDate

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val today = LocalDate.now()

    val icsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* 결과 처리 불필요 */ }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("설정") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── 달력 설정 ──────────────────────────────────────
            SettingSection("달력") {
                SwitchRow("음력 표시", uiState.showLunar, viewModel::setShowLunar)
                SwitchRow("공휴일 표시", uiState.showHoliday, viewModel::setShowHoliday)
                SwitchRow("다크 모드", uiState.darkMode, viewModel::setDarkMode)
            }

            // ── 알림 설정 ──────────────────────────────────────
            SettingSection("알림") {
                SwitchRow("근무 알림 활성화", uiState.notificationEnabled, viewModel::setNotificationEnabled)

                if (uiState.notificationEnabled) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("알람 목록", style = MaterialTheme.typography.titleSmall)
                        TextButton(onClick = viewModel::showAddAlarmDialog) {
                            Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                            Text("추가", fontSize = 12.sp)
                        }
                    }

                    uiState.alarms.forEach { alarm ->
                        AlarmRow(
                            alarm    = alarm,
                            onToggle = { viewModel.toggleAlarm(alarm) },
                            onDelete = { viewModel.deleteAlarm(alarm) }
                        )
                    }

                    if (uiState.alarms.isEmpty()) {
                        Text(
                            text  = "설정된 알람이 없습니다",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            // ── 삼성 캘린더 내보내기 ──────────────────────────
            SettingSection("캘린더 연동 (ICS 내보내기)") {
                Text(
                    text  = "ICS 파일을 내보내면 삼성 캘린더, 구글 캘린더 등에서 가져오기로 일정을 등록할 수 있습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.exportCurrentMonthIcs(today.year, today.monthValue) { intent ->
                            intent?.let { icsLauncher.launch(it) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("이번 달 ICS 내보내기 (${today.year}년 ${today.monthValue}월)")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.exportYearIcs(today.year) { intent ->
                            intent?.let { icsLauncher.launch(it) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("올해 전체 ICS 내보내기 (${today.year}년)")
                }

                uiState.exportResult?.let { msg ->
                    LaunchedEffect(msg) { viewModel.clearExportResult() }
                    Text(
                        text  = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ── 앱 정보 ────────────────────────────────────────
            SettingSection("앱 정보") {
                InfoRow("버전", "1.0.0")
                InfoRow("개발", "오늘근무 클론")
                InfoRow("데이터", "기기 로컬 저장 (서버 미사용)")
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // ── 알람 추가 다이얼로그 ──────────────────────────────────
    if (uiState.showAddAlarmDialog) {
        AddAlarmDialog(
            onConfirm = { label, mins, filter ->
                viewModel.addAlarm(label, mins, filter)
            },
            onDismiss = viewModel::hideAddAlarmDialog
        )
    }
}

// ── 섹션 ─────────────────────────────────────────────────────
@Composable
private fun SettingSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text   = title,
            style  = MaterialTheme.typography.titleSmall,
            color  = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier  = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                content()
            }
        }
    }
}

// ── 스위치 행 ─────────────────────────────────────────────────
@Composable
private fun SwitchRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = value, onCheckedChange = onChange)
    }
}

// ── 정보 행 ───────────────────────────────────────────────────
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label, modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

// ── 알람 행 ───────────────────────────────────────────────────
@Composable
private fun AlarmRow(
    alarm: AlarmSettingEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(alarm.label, style = MaterialTheme.typography.bodyMedium)
            Text(
                buildString {
                    append("${alarm.minutesBefore}분 전")
                    if (alarm.shiftTypeFilter.isNotBlank()) append(" · ${alarm.shiftTypeFilter}")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        Switch(checked = alarm.isEnabled, onCheckedChange = { onToggle() })
        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Delete, "삭제",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.error)
        }
    }
}

// ── 알람 추가 다이얼로그 ──────────────────────────────────────
@Composable
private fun AddAlarmDialog(
    onConfirm: (label: String, minutesBefore: Int, shiftTypeFilter: String) -> Unit,
    onDismiss: () -> Unit
) {
    var label          by remember { mutableStateOf("출근 알람") }
    var minutesBefore  by remember { mutableStateOf("60") }
    var filterExpanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf<ShiftType?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("알람 추가") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("알람 이름") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = minutesBefore,
                    onValueChange = { minutesBefore = it },
                    label = { Text("근무 시작 몇 분 전") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 빠른 선택
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(10, 30, 60, 120).forEach { mins ->
                        FilterChip(
                            selected = minutesBefore == mins.toString(),
                            onClick  = { minutesBefore = mins.toString() },
                            label    = { Text("${mins}분") }
                        )
                    }
                }

                // 근무 타입 필터
                ExposedDropdownMenuBox(
                    expanded = filterExpanded,
                    onExpandedChange = { filterExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedFilter?.label ?: "전체 근무",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("근무 타입 (선택)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(filterExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    ExposedDropdownMenu(
                        expanded = filterExpanded,
                        onDismissRequest = { filterExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("전체 근무") },
                            onClick = { selectedFilter = null; filterExpanded = false }
                        )
                        ShiftType.entries.filter { it.isWorkDay }.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label) },
                                onClick = { selectedFilter = type; filterExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val mins = minutesBefore.toIntOrNull() ?: 60
                onConfirm(label, mins, selectedFilter?.name ?: "")
            }) { Text("추가") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}
