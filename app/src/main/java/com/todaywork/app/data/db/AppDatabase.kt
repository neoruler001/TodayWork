package com.todaywork.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.todaywork.app.data.db.dao.*
import com.todaywork.app.data.db.entity.*

@Database(
    entities = [
        ShiftPatternEntity::class,
        WorkRecordEntity::class,
        SalarySettingEntity::class,
        AlarmSettingEntity::class,
        MemoEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shiftPatternDao(): ShiftPatternDao
    abstract fun workRecordDao(): WorkRecordDao
    abstract fun salarySettingDao(): SalarySettingDao
    abstract fun alarmSettingDao(): AlarmSettingDao
    abstract fun memoDao(): MemoDao
}
