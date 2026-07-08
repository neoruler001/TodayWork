package com.todaywork.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 날짜별 실제 근무 기록
 * 패턴으로 생성된 기본값 + 수동 수정 내역 보관
 */
@Entity(tableName = "work_records")
data class WorkRecordEntity(
    @PrimaryKey val dateEpoch: Long,     // 날짜 (epoch days, 기본키)
    val shiftTypeName: String,           // ShiftType 이름
    val startTimeMinutes: Int = 0,       // 근무 시작 (분, 자정 기준)
    val endTimeMinutes: Int = 0,         // 근무 종료 (분, 자정 기준)
    val memo: String = "",               // 메모
    val isManual: Boolean = false,       // 수동 입력/수정 여부
    val patternId: Long = -1L,          // 어느 패턴에서 생성됐는지
    val updatedAt: Long = System.currentTimeMillis()
)
