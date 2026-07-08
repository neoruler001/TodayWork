package com.todaywork.app.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.todaywork.app.data.db.entity.ShiftPatternEntity
import com.todaywork.app.data.model.ShiftType
import com.todaywork.app.data.repository.ShiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ScheduleUiState(
    val patterns: List<ShiftPatternEntity> = emptyList(),
    val activePatternId: Long = -1L,
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val shiftRepo: ShiftRepository,
    private val gson: Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            shiftRepo.getAllPatterns().collect { patterns ->
                val activeId = patterns.find { it.isActive }?.id ?: -1L
                _uiState.update { it.copy(patterns = patterns, activePatternId = activeId) }
            }
        }
    }

    fun addPattern(name: String, cycles: List<ShiftType>, startDate: LocalDate, offsetDay: Int) {
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "패턴 이름을 입력하세요") }
            return
        }
        if (cycles.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "사이클을 최소 1개 이상 추가하세요") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showAddDialog = false) }
            try {
                val id = shiftRepo.addPattern(name, cycles, startDate, offsetDay)
                shiftRepo.activatePattern(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "패턴 추가 실패: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun activatePattern(patternId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                shiftRepo.activatePattern(patternId)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deletePattern(patternId: Long) {
        viewModelScope.launch {
            shiftRepo.deletePattern(patternId)
        }
    }

    fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true) }
    fun hideAddDialog() = _uiState.update { it.copy(showAddDialog = false) }
    fun clearError()    = _uiState.update { it.copy(errorMessage = null) }

    fun parseCycles(pattern: ShiftPatternEntity): List<ShiftType> {
        return try {
            val names: List<String> = gson.fromJson(
                pattern.cyclesJson,
                object : TypeToken<List<String>>() {}.type
            )
            names.mapNotNull { runCatching { ShiftType.valueOf(it) }.getOrNull() }
        } catch (e: Exception) { emptyList() }
    }

    /** 사전 정의 교대 패턴 프리셋 */
    val presets: List<Pair<String, List<ShiftType>>> = listOf(
        "주야휴휴"     to listOf(ShiftType.DAY,  ShiftType.NIGHT, ShiftType.REST, ShiftType.REST),
        "주야비휴"     to listOf(ShiftType.DAY,  ShiftType.NIGHT, ShiftType.OFF,  ShiftType.REST),
        "주주야야휴휴" to listOf(ShiftType.DAY,  ShiftType.DAY,   ShiftType.NIGHT, ShiftType.NIGHT, ShiftType.REST, ShiftType.REST),
        "주주휴휴야야휴휴" to listOf(ShiftType.DAY, ShiftType.DAY, ShiftType.REST, ShiftType.REST, ShiftType.NIGHT, ShiftType.NIGHT, ShiftType.REST, ShiftType.REST),
        "4조3교대(주야휴비)" to listOf(ShiftType.DAY, ShiftType.NIGHT, ShiftType.REST, ShiftType.OFF),
        "5조3교대"     to listOf(ShiftType.DAY, ShiftType.DAY, ShiftType.NIGHT, ShiftType.REST, ShiftType.OFF),
        "당직"         to listOf(ShiftType.DUTY, ShiftType.REST),
        "주5일"        to listOf(ShiftType.DAY, ShiftType.DAY, ShiftType.DAY, ShiftType.DAY, ShiftType.DAY, ShiftType.REST, ShiftType.REST)
    )
}
