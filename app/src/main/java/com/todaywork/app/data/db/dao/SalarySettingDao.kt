package com.todaywork.app.data.db.dao

import androidx.room.*
import com.todaywork.app.data.db.entity.SalarySettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalarySettingDao {

    @Query("SELECT * FROM salary_settings WHERE id = 1")
    fun getSalarySettings(): Flow<SalarySettingEntity?>

    @Query("SELECT * FROM salary_settings WHERE id = 1")
    suspend fun getSalarySettingsSync(): SalarySettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(setting: SalarySettingEntity)
}
