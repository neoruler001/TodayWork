package com.todaywork.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 교대 근무 패턴 Entity
 * cycles: ShiftType 이름 배열을 JSON 문자열로 저장
 * 예) 주야휴휴 → ["DAY","NIGHT","REST","REST"]
 */
@Entity(tableName = "shift_patterns")
data class ShiftPatternEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,                    // 패턴 이름 (예: "주야휴휴")
    val cyclesJson: String,              // 사이클 JSON 배열
    val startDateEpoch: Long,            // 패턴 시작 기준일 (epoch days)
    val cycleOffsetDay: Int = 0,         // 시작일 기준 사이클 내 오프셋
    val isActive: Boolean = true,        // 현재 활성 패턴 여부
    val createdAt: Long = System.currentTimeMillis()
)
