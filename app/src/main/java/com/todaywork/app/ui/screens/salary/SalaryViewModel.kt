package com.todaywork.app.ui.screens.salary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todaywork.app.data.db.entity.SalarySettingEntity
import com.todaywork.app.data.model.WorkSummary
import com.todaywork.app.data.repository.SalaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class SalaryUiState(
    val year: Int            = LocalDate.now().year,
    val month: Int           = LocalDate.now().monthValue,
    val summary: WorkSummary? = null,
    val settings: SalarySettingEntity? = null,
    val isLoading: Boolean   = false,
    val showSettingsDialog: Boolean = false
)

@HiltViewModel
class SalaryViewModel @Inject constructor(
    private val salaryRepo: SalaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalaryUiState())
    val uiState: StateFlow<SalaryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            salaryRepo.getSalarySettings().collect { setting ->
                _uiState.update { it.copy(settings = setting) }
            }
        }
        loadSummary(_uiState.value.year, _uiState.value.month)
    }

    fun goToPreviousMonth() {
        val d = LocalDate.of(_uiState.value.year, _uiState.value.month, 1).minusMonths(1)
        changeMonth(d.year, d.monthValue)
    }

    fun goToNextMonth() {
        val d = LocalDate.of(_uiState.value.year, _uiState.value.month, 1).plusMonths(1)
        changeMonth(d.year, d.monthValue)
    }

    private fun changeMonth(year: Int, month: Int) {
        _uiState.update { it.copy(year = year, month = month) }
        loadSummary(year, month)
    }

    private fun loadSummary(year: Int, month: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val summary = salaryRepo.calculateMonthlySummary(year, month)
                _uiState.update { it.copy(summary = summary, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun saveSalarySettings(
        hourlyWage: Int,
        overtimeRate: Float,
        nightRate: Float,
        weekendRate: Float,
        holidayRate: Float,
        mealAllowance: Int,
        regularHoursPerDay: Int
    ) {
        viewModelScope.launch {
            val setting = SalarySettingEntity(
                hourlyWage           = hourlyWage,
                overtimeRate         = overtimeRate,
                nightShiftRate       = nightRate,
                weekendRate          = weekendRate,
                holidayRate          = holidayRate,
                mealAllowancePerDay  = mealAllowance,
                regularHoursPerDay   = regularHoursPerDay
            )
            salaryRepo.saveSalarySettings(setting)
            loadSummary(_uiState.value.year, _uiState.value.month)
        }
    }

    fun showSettings() = _uiState.update { it.copy(showSettingsDialog = true) }
    fun hideSettings() = _uiState.update { it.copy(showSettingsDialog = false) }
}
