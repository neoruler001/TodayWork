package com.todaywork.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 급여 계산 설정
 * 단일 행 테이블 (id = 1 고정)
 */
@Entity(tableName = "salary_settings")
data class SalarySettingEntity(
    @PrimaryKey val id: Int = 1,
    val hourlyWage: Int = 9860,          // 시급 (2024년 최저임금 기본값)
    val overtimeRate: Float = 1.5f,      // 연장수당 배율
    val nightShiftRate: Float = 1.5f,    // 야간수당 배율 (22:00~06:00)
    val weekendRate: Float = 1.5f,       // 주말수당 배율
    val holidayRate: Float = 2.0f,       // 공휴일수당 배율
    val mealAllowancePerDay: Int = 5000, // 1일 식대
    val regularHoursPerDay: Int = 8,     // 1일 기본 근무시간
    val regularHoursPerWeek: Int = 40,   // 주 기본 근무시간
    val updatedAt: Long = System.currentTimeMillis()
)
