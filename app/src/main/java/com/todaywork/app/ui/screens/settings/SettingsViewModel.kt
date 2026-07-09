package com.todaywork.app.ui.screens.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todaywork.app.data.datastore.AppPreferences
import com.todaywork.app.data.db.dao.AlarmSettingDao
import com.todaywork.app.data.db.dao.WorkRecordDao
import com.todaywork.app.data.db.entity.AlarmSettingEntity
import com.todaywork.app.data.model.DayInfo
import com.todaywork.app.data.repository.ShiftRepository
import com.todaywork.app.util.IcsExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class SettingsUiState(
    val showLunar: Boolean       = false,
    val showHoliday: Boolean     = true,
    val darkMode: Boolean        = false,
    val startMonday: Boolean     = false,
    val notificationEnabled: Boolean = true,
    val alarms: List<AlarmSettingEntity> = emptyList(),
    val exportResult: String?    = null,
    val showAddAlarmDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: AppPreferences,
    private val alarmSettingDao: AlarmSettingDao,
    private val shiftRepo: ShiftRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefs.showLunar,
                prefs.showHoliday,
                prefs.darkMode,
                prefs.calendarStartMonday,
                prefs.notificationEnabled
            ) { lunar, holiday, dark, monday, notif ->
                _uiState.update {
                    it.copy(
                        showLunar = lunar, showHoliday = holiday,
                        darkMode = dark, startMonday = monday,
                        notificationEnabled = notif
                    )
                }
            }.collect()
        }
        viewModelScope.launch {
            alarmSettingDao.getAllAlarms().collect { alarms ->
                _uiState.update { it.copy(alarms = alarms) }
            }
        }
    }

    fun setShowLunar(v: Boolean)    = viewModelScope.launch { prefs.setShowLunar(v) }
    fun setShowHoliday(v: Boolean)  = viewModelScope.launch { prefs.setShowHoliday(v) }
    fun setDarkMode(v: Boolean)     = viewModelScope.launch { prefs.setDarkMode(v) }
    fun setStartMonday(v: Boolean)  = viewModelScope.launch { prefs.setCalendarStartMonday(v) }
    fun setNotificationEnabled(v: Boolean) = viewModelScope.launch { prefs.setNotificationEnabled(v) }

    fun addAlarm(label: String, minutesBefore: Int, shiftTypeFilter: String) {
        viewModelScope.launch {
            alarmSettingDao.insert(
                AlarmSettingEntity(
                    label = label,
                    minutesBefore = minutesBefore,
                    shiftTypeFilter = shiftTypeFilter
                )
            )
            _uiState.update { it.copy(showAddAlarmDialog = false) }
        }
    }

    fun toggleAlarm(alarm: AlarmSettingEntity) {
        viewModelScope.launch {
            alarmSettingDao.setEnabled(alarm.id, !alarm.isEnabled)
        }
    }

    fun deleteAlarm(alarm: AlarmSettingEntity) {
        viewModelScope.launch { alarmSettingDao.delete(alarm) }
    }

    /**
     * 현재 월 ICS 내보내기 → Intent 반환
     */
    fun exportCurrentMonthIcs(year: Int, month: Int, onResult: (Intent?) -> Unit) {
        viewModelScope.launch {
            try {
                val days: List<DayInfo> = shiftRepo.getDayInfosForMonth(year, month)
                val intent = IcsExporter.exportAndShare(context, days, "${year}년${month}월")
                onResult(intent)
                _uiState.update { it.copy(exportResult = "ICS 내보내기 성공") }
            } catch (e: Exception) {
                _uiState.update { it.copy(exportResult = "내보내기 실패: ${e.message}") }
                onResult(null)
            }
        }
    }

    /**
     * 전체 1년 ICS 내보내기
     */
    fun exportYearIcs(year: Int, onResult: (Intent?) -> Unit) {
        viewModelScope.launch {
            try {
                val allDays = mutableListOf<DayInfo>()
                for (m in 1..12) {
                    allDays.addAll(shiftRepo.getDayInfosForMonth(year, m))
                }
                val intent = IcsExporter.exportAndShare(context, allDays, "${year}년_전체")
                onResult(intent)
                _uiState.update { it.copy(exportResult = "ICS 내보내기 성공") }
            } catch (e: Exception) {
                _uiState.update { it.copy(exportResult = "내보내기 실패: ${e.message}") }
                onResult(null)
            }
        }
    }

    fun showAddAlarmDialog() = _uiState.update { it.copy(showAddAlarmDialog = true) }
    fun hideAddAlarmDialog() = _uiState.update { it.copy(showAddAlarmDialog = false) }
    fun clearExportResult()  = _uiState.update { it.copy(exportResult = null) }
}
