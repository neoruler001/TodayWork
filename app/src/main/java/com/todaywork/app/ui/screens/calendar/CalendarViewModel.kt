package com.todaywork.app.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todaywork.app.data.datastore.AppPreferences
import com.todaywork.app.data.db.entity.ShiftPatternEntity
import com.todaywork.app.data.model.DayInfo
import com.todaywork.app.data.repository.ShiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CalendarUiState(
    val year: Int            = LocalDate.now().year,
    val month: Int           = LocalDate.now().monthValue,
    val days: List<DayInfo>  = emptyList(),
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean   = false,
    val showLunar: Boolean   = true,
    val showHoliday: Boolean = true,
    val patterns: List<ShiftPatternEntity> = emptyList()
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val shiftRepo: ShiftRepository,
    private val prefs: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadMonth(_uiState.value.year, _uiState.value.month)
        viewModelScope.launch {
            shiftRepo.getAllPatterns().collect { patterns ->
                _uiState.update { it.copy(patterns = patterns) }
            }
        }
        viewModelScope.launch {
            combine(
                prefs.showLunar,
                prefs.showHoliday
            ) { lunar, holiday ->
                lunar to holiday
            }.collect { (lunar, holiday) ->
                _uiState.update { it.copy(showLunar = lunar, showHoliday = holiday) }
            }
        }
    }

    fun goToPreviousMonth() {
        val current = LocalDate.of(_uiState.value.year, _uiState.value.month, 1)
        val prev = current.minusMonths(1)
        changeMonth(prev.year, prev.monthValue)
    }

    fun goToNextMonth() {
        val current = LocalDate.of(_uiState.value.year, _uiState.value.month, 1)
        val next = current.plusMonths(1)
        changeMonth(next.year, next.monthValue)
    }

    fun goToToday() {
        val today = LocalDate.now()
        changeMonth(today.year, today.monthValue)
        _uiState.update { it.copy(selectedDate = today) }
    }

    fun goToDate(date: LocalDate) {
        changeMonth(date.year, date.monthValue)
        _uiState.update { it.copy(selectedDate = date) }
    }

    private fun changeMonth(year: Int, month: Int) {
        _uiState.update { it.copy(year = year, month = month) }
        loadMonth(year, month)
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedDate = null) }
    }

    fun saveWorkRecord(
        date: LocalDate,
        shiftTypeName: String,
        startHour: Int,
        startMin: Int,
        endHour: Int,
        endMin: Int
    ) {
        viewModelScope.launch {
            val shiftType = runCatching {
                com.todaywork.app.data.model.ShiftType.valueOf(shiftTypeName)
            }.getOrElse { com.todaywork.app.data.model.ShiftType.REST }

            shiftRepo.updateWorkRecord(
                date = date,
                shiftType = shiftType,
                startTimeMinutes = startHour * 60 + startMin,
                endTimeMinutes = endHour * 60 + endMin
            )
            if (shiftType == com.todaywork.app.data.model.ShiftType.HEALTH_OFF) {
                val existingMemos = shiftRepo.getMemosForDate(date)
                if (existingMemos.none { it.title == "보건" }) {
                    shiftRepo.addMemo(date, "보건", 0xFF26C6DAL, 0, 0, true)
                }
            }
            loadMonth(_uiState.value.year, _uiState.value.month)
        }
    }

    fun resetDateToPattern(date: LocalDate) {
        viewModelScope.launch {
            shiftRepo.resetToPattern(date)
            loadMonth(_uiState.value.year, _uiState.value.month)
        }
    }

    fun applyPatternToDateRange(patternId: Long, startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                shiftRepo.applyPatternToDateRange(patternId, startDate, endDate)
            } finally {
                loadMonth(_uiState.value.year, _uiState.value.month)
            }
        }
    }

    fun addMemo(
        date: LocalDate,
        title: String,
        colorHex: Long,
        startTimeMinutes: Int,
        endTimeMinutes: Int,
        isAllDay: Boolean
    ) {
        viewModelScope.launch {
            shiftRepo.addMemo(date, title, colorHex, startTimeMinutes, endTimeMinutes, isAllDay)
            loadMonth(_uiState.value.year, _uiState.value.month)
        }
    }

    fun deleteMemo(memoId: Long) {
        viewModelScope.launch {
            shiftRepo.deleteMemo(memoId)
            loadMonth(_uiState.value.year, _uiState.value.month)
        }
    }

    fun updateMemo(
        memoId: Long,
        date: LocalDate,
        title: String,
        colorHex: Long,
        startTimeMinutes: Int,
        endTimeMinutes: Int,
        isAllDay: Boolean
    ) {
        viewModelScope.launch {
            val item = com.todaywork.app.data.model.MemoItem(
                id = memoId,
                title = title,
                colorHex = colorHex,
                startTimeMinutes = startTimeMinutes,
                endTimeMinutes = endTimeMinutes,
                isAllDay = isAllDay
            )
            shiftRepo.updateMemo(item, date)
            loadMonth(_uiState.value.year, _uiState.value.month)
        }
    }

    private fun loadMonth(year: Int, month: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val days = shiftRepo.getDayInfosForMonth(year, month)
                _uiState.update { it.copy(days = days, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadMonthForDate(date: LocalDate) {
        if (date.year == _uiState.value.year && date.monthValue == _uiState.value.month) return
        _uiState.update { it.copy(year = date.year, month = date.monthValue) }
        loadMonth(date.year, date.monthValue)
    }

    fun getSelectedDayInfo(): DayInfo? {
        val selected = _uiState.value.selectedDate ?: return null
        return _uiState.value.days.find { it.date == selected }
    }
}
