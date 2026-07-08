package com.todaywork.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 근무 알람 설정
 */
@Entity(tableName = "alarm_settings")
data class AlarmSettingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,                   // 알람 이름 (예: "출근 1시간 전")
    val minutesBefore: Int,              // 근무 시작 몇 분 전 알람
    val isEnabled: Boolean = true,
    val shiftTypeFilter: String = "",    // 특정 근무 타입만 알람 (빈 문자열=전체)
    val createdAt: Long = System.currentTimeMillis()
)
