package com.todaywork.app.data.model

/**
 * 월별 근무 요약 데이터 모델
 */
data class WorkSummary(
    val year: Int,
    val month: Int,
    val totalWorkDays: Int = 0,          // 실제 근무일 수
    val totalWorkHours: Double = 0.0,    // 총 근무시간
    val regularHours: Double = 0.0,      // 정규 근무시간
    val overtimeHours: Double = 0.0,     // 연장 근무시간
    val nightHours: Double = 0.0,        // 야간 근무시간
    val weekendHours: Double = 0.0,      // 주말 근무시간
    val annualDaysUsed: Int = 0,         // 연차 사용일수
    val halfDaysUsed: Int = 0,           // 반차 사용일수 (0.5일 단위)
    val baseSalary: Long = 0L,           // 기본급
    val overtimePay: Long = 0L,          // 연장수당
    val nightPay: Long = 0L,             // 야간수당
    val weekendPay: Long = 0L,           // 주말수당
    val mealAllowance: Long = 0L,        // 식대
    val totalPay: Long = 0L              // 합계
)
