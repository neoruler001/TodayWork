package com.todaywork.app.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.todaywork.app.data.datastore.AppPreferences
import com.todaywork.app.data.db.dao.AlarmSettingDao
import com.todaywork.app.data.db.dao.MemoDao
import com.todaywork.app.data.db.dao.ShiftPatternDao
import com.todaywork.app.data.db.dao.WorkRecordDao
import com.todaywork.app.data.db.entity.AlarmSettingEntity
import com.todaywork.app.data.model.DayInfo
import com.todaywork.app.data.repository.ShiftRepository
import com.todaywork.app.util.BackupData
import com.todaywork.app.util.BackupManager
import com.todaywork.app.util.IcsExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class SettingsUiState(
    val showLunar: Boolean           = false,
    val showHoliday: Boolean         = true,
    val darkMode: Boolean            = false,
    val startMonday: Boolean         = false,
    val notificationEnabled: Boolean = true,
    val alarms: List<AlarmSettingEntity> = emptyList(),
    val exportResult: String?        = null,
    val showAddAlarmDialog: Boolean  = false,
    val backupStatus: String?        = null,
    val isBackupInProgress: Boolean  = false,
    val showRestoreConfirmDialog: Boolean = false,
    val pendingRestoreUri: Uri?      = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: AppPreferences,
    private val alarmSettingDao: AlarmSettingDao,
    private val shiftPatternDao: ShiftPatternDao,
    private val workRecordDao: WorkRecordDao,
    private val memoDao: MemoDao,
    private val shiftRepo: ShiftRepository,
    private val gson: Gson
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
        viewModelScope.launch { alarmSettingDao.setEnabled(alarm.id, !alarm.isEnabled) }
    }

    fun deleteAlarm(alarm: AlarmSettingEntity) {
        viewModelScope.launch { alarmSettingDao.delete(alarm) }
    }

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

    fun exportYearIcs(year: Int, onResult: (Intent?) -> Unit) {
        viewModelScope.launch {
            try {
                val allDays = mutableListOf<DayInfo>()
                for (m in 1..12) allDays.addAll(shiftRepo.getDayInfosForMonth(year, m))
                val intent = IcsExporter.exportAndShare(context, allDays, "${year}년_전체")
                onResult(intent)
                _uiState.update { it.copy(exportResult = "ICS 내보내기 성공") }
            } catch (e: Exception) {
                _uiState.update { it.copy(exportResult = "내보내기 실패: ${e.message}") }
                onResult(null)
            }
        }
    }

    // ── 백업 ──────────────────────────────────────────────────────
    fun exportBackup(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isBackupInProgress = true, backupStatus = null) }
            try {
                val data = BackupData(
                    patterns    = shiftPatternDao.getAllPatternsSync(),
                    workRecords = workRecordDao.getAllWorkRecords(),
                    memos       = memoDao.getAllMemos(),
                    alarms      = alarmSettingDao.getAllAlarmsSync()
                )
                BackupManager.write(context, uri, data, gson)
                _uiState.update { it.copy(isBackupInProgress = false, backupStatus = "백업 완료") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isBackupInProgress = false, backupStatus = "백업 실패: ${e.message}") }
            }
        }
    }

    // ── 복원 (확인 다이얼로그 먼저) ────────────────────────────────
    fun requestRestore(uri: Uri) {
        _uiState.update { it.copy(showRestoreConfirmDialog = true, pendingRestoreUri = uri) }
    }

    fun confirmRestore() {
        val uri = _uiState.value.pendingRestoreUri ?: return
        _uiState.update { it.copy(showRestoreConfirmDialog = false, pendingRestoreUri = null) }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isBackupInProgress = true, backupStatus = null) }
            try {
                val data = BackupManager.read(context, uri, gson)
                data.patterns.forEach    { shiftPatternDao.insertPattern(it) }
                data.workRecords.forEach { workRecordDao.insertOrUpdate(it) }
                data.memos.forEach       { memoDao.insertMemo(it) }
                data.alarms.forEach      { alarmSettingDao.insert(it) }
                val counts = "패턴 ${data.patterns.size}개, 근무기록 ${data.workRecords.size}개, " +
                        "메모 ${data.memos.size}개, 알람 ${data.alarms.size}개"
                _uiState.update { it.copy(isBackupInProgress = false, backupStatus = "복원 완료 ($counts)") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isBackupInProgress = false, backupStatus = "복원 실패: ${e.message}") }
            }
        }
    }

    fun cancelRestore() {
        _uiState.update { it.copy(showRestoreConfirmDialog = false, pendingRestoreUri = null) }
    }

    fun showAddAlarmDialog() = _uiState.update { it.copy(showAddAlarmDialog = true) }
    fun hideAddAlarmDialog() = _uiState.update { it.copy(showAddAlarmDialog = false) }
    fun clearExportResult()  = _uiState.update { it.copy(exportResult = null) }
    fun clearBackupStatus()  = _uiState.update { it.copy(backupStatus = null) }
}
