package com.todaywork.app.ui.screens.salary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.todaywork.app.data.db.entity.SalarySettingEntity
import com.todaywork.app.data.model.WorkSummary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SalaryScreen(
    viewModel: SalaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val numFmt = NumberFormat.getNumberInstance(Locale.KOREA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("급여 계산") },
                actions = {
                    IconButton(onClick = viewModel::showSettings) {
                        Icon(Icons.Default.Settings, "급여 설정")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── 월 네비게이션 ───────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::goToPreviousMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "이전 달")
                }
                Text(
                    text      = "${uiState.year}년 ${uiState.month}월",
                    style     = MaterialTheme.typography.titleMedium,
                    modifier  = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = viewModel::goToNextMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "다음 달")
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val summary = uiState.summary

                if (summary == null || summary.totalWorkDays == 0) {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text  = "이 달에 등록된 근무 기록이 없습니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // ── 총 예상 급여 ──────────────────────────────
                    TotalPayCard(totalPay = summary.totalPay, numFmt = numFmt)

                    Spacer(Modifier.height(12.dp))

                    // ── 근무 요약 ────────────────────────────────
                    WorkSummaryCard(summary = summary)

                    Spacer(Modifier.height(12.dp))

                    // ── 급여 내역 ────────────────────────────────
                    PayBreakdownCard(summary = summary, numFmt = numFmt)

                    Spacer(Modifier.height(12.dp))

                    // ── 연차/반차 현황 ───────────────────────────
                    LeaveCard(summary = summary)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── 급여 설정 다이얼로그 ──────────────────────────────────
    if (uiState.showSettingsDialog) {
        SalarySettingsDialog(
            current   = uiState.settings ?: SalarySettingEntity(),
            onSave    = { hw, ot, nr, wr, hr, ma, rhd ->
                viewModel.saveSalarySettings(hw, ot, nr, wr, hr, ma, rhd)
                viewModel.hideSettings()
            },
            onDismiss = viewModel::hideSettings
        )
    }
}

@Composable
private fun TotalPayCard(totalPay: Long, numFmt: NumberFormat) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text  = "이번 달 예상 급여",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text       = "₩ ${numFmt.format(totalPay)}",
                fontSize   = 32.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun WorkSummaryCard(summary: WorkSummary) {
    SectionCard(title = "근무 요약") {
        val rows = listOf(
            "근무일 수"   to "${summary.totalWorkDays}일",
            "총 근무시간" to "%.1f시간".format(summary.totalWorkHours),
            "정규 시간"   to "%.1f시간".format(summary.regularHours),
            "연장 시간"   to "%.1f시간".format(summary.overtimeHours),
            "야간 시간"   to "%.1f시간".format(summary.nightHours),
            "주말 시간"   to "%.1f시간".format(summary.weekendHours)
        )
        rows.forEach { (label, value) ->
            SummaryRow(label = label, value = value)
        }
    }
}

@Composable
private fun PayBreakdownCard(summary: WorkSummary, numFmt: NumberFormat) {
    SectionCard(title = "급여 내역") {
        val rows = listOf(
            "기본급"   to summary.baseSalary,
            "연장수당" to summary.overtimePay,
            "야간수당" to summary.nightPay,
            "주말수당" to summary.weekendPay,
            "식대"     to summary.mealAllowance
        )
        rows.forEach { (label, amount) ->
            SummaryRow(
                label = label,
                value = "₩ ${numFmt.format(amount)}",
                valueColor = if (amount > 0)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        SummaryRow(
            label      = "합계",
            value      = "₩ ${numFmt.format(summary.totalPay)}",
            labelWeight = FontWeight.Bold,
            valueWeight = FontWeight.Bold,
            valueColor  = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun LeaveCard(summary: WorkSummary) {
    if (summary.annualDaysUsed == 0 && summary.halfDaysUsed == 0) return
    SectionCard(title = "휴가 현황") {
        if (summary.annualDaysUsed > 0) {
            SummaryRow(label = "연차 사용", value = "${summary.annualDaysUsed}일")
        }
        if (summary.halfDaysUsed > 0) {
            SummaryRow(label = "반차 사용", value = "${summary.halfDaysUsed}회 (${summary.halfDaysUsed * 0.5}일)")
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text   = title,
                style  = MaterialTheme.typography.titleSmall,
                color  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    labelWeight: FontWeight = FontWeight.Normal,
    valueWeight: FontWeight = FontWeight.Normal,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = labelWeight,
            modifier   = Modifier.weight(1f),
            color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = valueWeight,
            color      = valueColor
        )
    }
}

// ── 급여 설정 다이얼로그 ──────────────────────────────────────
@Composable
private fun SalarySettingsDialog(
    current: SalarySettingEntity,
    onSave: (Int, Float, Float, Float, Float, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var hourlyWage by remember { mutableStateOf(current.hourlyWage.toString()) }
    var overtimeRate by remember { mutableStateOf(current.overtimeRate.toString()) }
    var nightRate    by remember { mutableStateOf(current.nightShiftRate.toString()) }
    var weekendRate  by remember { mutableStateOf(current.weekendRate.toString()) }
    var holidayRate  by remember { mutableStateOf(current.holidayRate.toString()) }
    var mealAllowance by remember { mutableStateOf(current.mealAllowancePerDay.toString()) }
    var regularHours  by remember { mutableStateOf(current.regularHoursPerDay.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("급여 설정") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                NumberField("시급 (원)", hourlyWage) { hourlyWage = it }
                NumberField("연장수당 배율 (예: 1.5)", overtimeRate) { overtimeRate = it }
                NumberField("야간수당 배율 (예: 1.5)", nightRate)    { nightRate    = it }
                NumberField("주말수당 배율 (예: 1.5)", weekendRate)   { weekendRate  = it }
                NumberField("공휴일수당 배율 (예: 2.0)", holidayRate) { holidayRate  = it }
                NumberField("1일 식대 (원)", mealAllowance)           { mealAllowance = it }
                NumberField("1일 기본 근무시간 (시간)", regularHours) { regularHours = it }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    hourlyWage.toIntOrNull()   ?: current.hourlyWage,
                    overtimeRate.toFloatOrNull() ?: current.overtimeRate,
                    nightRate.toFloatOrNull()    ?: current.nightShiftRate,
                    weekendRate.toFloatOrNull()  ?: current.weekendRate,
                    holidayRate.toFloatOrNull()  ?: current.holidayRate,
                    mealAllowance.toIntOrNull() ?: current.mealAllowancePerDay,
                    regularHours.toIntOrNull()  ?: current.regularHoursPerDay
                )
            }) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
