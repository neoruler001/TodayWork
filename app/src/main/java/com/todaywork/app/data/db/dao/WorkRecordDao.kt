package com.todaywork.app.data.db.dao

import androidx.room.*
import com.todaywork.app.data.db.entity.WorkRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkRecordDao {

    @Query("SELECT * FROM work_records WHERE dateEpoch BETWEEN :startEpoch AND :endEpoch ORDER BY dateEpoch ASC")
    fun getRecordsBetween(startEpoch: Long, endEpoch: Long): Flow<List<WorkRecordEntity>>

    @Query("SELECT * FROM work_records WHERE dateEpoch BETWEEN :startEpoch AND :endEpoch ORDER BY dateEpoch ASC")
    suspend fun getRecordsBetweenSync(startEpoch: Long, endEpoch: Long): List<WorkRecordEntity>

    @Query("SELECT * FROM work_records WHERE dateEpoch = :dateEpoch")
    suspend fun getRecordByDate(dateEpoch: Long): WorkRecordEntity?

    @Query("SELECT * FROM work_records WHERE dateEpoch = :dateEpoch")
    fun getRecordByDateFlow(dateEpoch: Long): Flow<WorkRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: WorkRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<WorkRecordEntity>)

    @Delete
    suspend fun deleteRecord(record: WorkRecordEntity)

    @Query("DELETE FROM work_records WHERE dateEpoch = :dateEpoch")
    suspend fun deleteByDate(dateEpoch: Long)

    @Query("DELETE FROM work_records WHERE patternId = :patternId AND isManual = 0")
    suspend fun deletePatternGeneratedRecords(patternId: Long)

    @Query("SELECT COUNT(*) FROM work_records WHERE shiftTypeName IN (:types) AND dateEpoch BETWEEN :startEpoch AND :endEpoch")
    suspend fun countByTypesAndRange(types: List<String>, startEpoch: Long, endEpoch: Long): Int
}
