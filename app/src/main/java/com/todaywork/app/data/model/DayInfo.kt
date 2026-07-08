package com.todaywork.app.data.model

import java.time.LocalDate

data class DayInfo(
    val date: LocalDate,
    val shiftType: ShiftType?,
    val lunarDay: String = "",
    val isHoliday: Boolean = false,
    val holidayName: String = "",
    val memos: List<MemoItem> = emptyList(),
    val startTime: String = "",
    val endTime: String = "",
    val isModified: Boolean = false
)
