package com.todaywork.app.data.db.dao

import androidx.room.*
import com.todaywork.app.data.db.entity.AlarmSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmSettingDao {

    @Query("SELECT * FROM alarm_settings ORDER BY minutesBefore DESC")
    fun getAllAlarms(): Flow<List<AlarmSettingEntity>>

    @Query("SELECT * FROM alarm_settings WHERE isEnabled = 1")
    suspend fun getEnabledAlarms(): List<AlarmSettingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: AlarmSettingEntity): Long

    @Update
    suspend fun update(alarm: AlarmSettingEntity)

    @Query("UPDATE alarm_settings SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Delete
    suspend fun delete(alarm: AlarmSettingEntity)
}
