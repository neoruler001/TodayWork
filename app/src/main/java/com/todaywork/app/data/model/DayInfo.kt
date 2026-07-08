package com.todaywork.app.data.model

import java.time.LocalDate

/**
 * 달력 하루 정보
 */
data class DayInfo(
    val date: LocalDate,
    val shiftType: ShiftType?,
    val lunarDay: String = "",       // 음력 표시 (예: "초하루", "15")
    val isHoliday: Boolean = false,
    val holidayName: String = "",
    val memo: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val isModified: Boolean = false  // 패턴 외 수동 수정 여부
)
