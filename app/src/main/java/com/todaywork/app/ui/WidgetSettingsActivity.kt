package com.todaywork.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.todaywork.app.data.datastore.WidgetPreferences
import com.todaywork.app.data.datastore.WidgetSettings
import com.todaywork.app.ui.theme.TodayWorkTheme
import com.todaywork.app.widget.CalendarWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetSettingsActivity : ComponentActivity() {

    @Inject
    lateinit var widgetPreferences: WidgetPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodayWorkTheme {
                WidgetSettingsScreen(
                    prefs  = widgetPreferences,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetSettingsScreen(
    prefs: WidgetPreferences,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    var settings by remember { mutableStateOf(WidgetSettings()) }

    LaunchedEffect(Unit) {
        settings = prefs.getSettings()
    }

    fun save(updated: WidgetSettings) {
        settings = updated
        scope.launch {
            prefs.save(updated)
            // 설정 저장 즉시 위젯 갱신
            try {
                val manager = GlanceAppWidgetManager(context)
                val ids = manager.getGlanceIds(CalendarWidget::class.java)
                ids.forEach { CalendarWidget().update(context, it) }
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("위젯 설정") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    // 배경 투명도
                    SettingRow(label = "배경 투명도") {
                        Slider(
                            value = settings.bgAlpha,
                            onValueChange = { save(settings.copy(bgAlpha = it)) },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    SettingDivider()

                    // 배경 색
                    SettingRowWithValue(
                        label = "배경 색",
                        value = when (settings.bgColor) {
                            "black" -> "검정"
                            "gray"  -> "회색"
                            "blue"  -> "파랑"
                            else    -> "흰색(기본)"
                        }
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "white" to Color.White,
                                "gray"  to Color(0xFFF5F5F5),
                                "blue"  to Color(0xFFE3F2FD),
                                "black" to Color(0xFF1C1C1C)
                            ).forEach { (key, color) ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(color)
                                        .border(
                                            width = if (settings.bgColor == key) 2.dp else 1.dp,
                                            color = if (settings.bgColor == key) MaterialTheme.colorScheme.primary else Color.LightGray,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { save(settings.copy(bgColor = key)) }
                                )
                            }
                        }
                    }
                    SettingDivider()

                    // 글자 크기
                    SettingRowToggle2(
                        label   = "글자 크기",
                        option1 = "기본",
                        option2 = "크게",
                        selected = if (settings.fontSizeLarge) 1 else 0,
                        onSelect = { save(settings.copy(fontSizeLarge = it == 1)) }
                    )
                    SettingDivider()

                    // 날짜 구분선
                    SettingRowToggle2(
                        label    = "날짜 구분선",
                        option1  = "보이기",
                        option2  = "감추기",
                        selected = if (settings.showDivider) 0 else 1,
                        onSelect = { save(settings.copy(showDivider = it == 0)) }
                    )
                    SettingDivider()

                    // 메모 크기
                    SettingRowToggle3(
                        label    = "메모 크기",
                        selected = settings.memoSize,
                        onSelect = { save(settings.copy(memoSize = it)) }
                    )
                    SettingDivider()

                    // 날짜, 요일 크기
                    SettingRowToggle3(
                        label    = "날짜, 요일 크기",
                        selected = settings.dateSize,
                        onSelect = { save(settings.copy(dateSize = it)) }
                    )
                    SettingDivider()

                    // 근무 크기
                    SettingRowToggle3(
                        label    = "근무 크기",
                        selected = settings.workSize,
                        onSelect = { save(settings.copy(workSize = it)) }
                    )
                    SettingDivider()

                    // 메모 작성
                    SettingRowSwitch(
                        label     = "메모 작성",
                        checked   = settings.memoWrite,
                        onChecked = { save(settings.copy(memoWrite = it)) }
                    )
                    SettingDivider()

                    // 음력 표시
                    SettingRowSwitch(
                        label     = "음력 표시",
                        checked   = settings.showLunar,
                        onChecked = { save(settings.copy(showLunar = it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color    = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun SettingRow(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun SettingRowWithValue(
    label: String,
    value: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun SettingRowSwitch(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun SettingRowToggle2(
    label: String,
    option1: String,
    option2: String,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToggleButton(text = option1, active = selected == 0) { onSelect(0) }
            ToggleButton(text = option2, active = selected == 1) { onSelect(1) }
        }
    }
}

@Composable
private fun SettingRowToggle3(
    label: String,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToggleButton(text = "1", active = selected == 1) { onSelect(1) }
            ToggleButton(text = "2", active = selected == 2) { onSelect(2) }
            ToggleButton(text = "3", active = selected == 3) { onSelect(3) }
        }
    }
}

@Composable
private fun ToggleButton(text: String, active: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(width = 56.dp, height = 36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (active) primary else Color(0xFFEEEEEE))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = text,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Medium,
            color      = if (active) Color.White else Color(0xFF555555)
        )
    }
}
