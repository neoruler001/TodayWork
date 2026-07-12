package com.todaywork.app.data.db.dao

import androidx.room.*
import com.todaywork.app.data.db.entity.ShiftPatternEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftPatternDao {

    @Query("SELECT * FROM shift_patterns ORDER BY createdAt DESC")
    fun getAllPatterns(): Flow<List<ShiftPatternEntity>>

    @Query("SELECT * FROM shift_patterns WHERE isActive = 1 LIMIT 1")
    fun getActivePattern(): Flow<ShiftPatternEntity?>

    @Query("SELECT * FROM shift_patterns WHERE id = :id")
    suspend fun getPatternById(id: Long): ShiftPatternEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: ShiftPatternEntity): Long

    @Update
    suspend fun updatePattern(pattern: ShiftPatternEntity)

    @Query("UPDATE shift_patterns SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE shift_patterns SET isActive = 1 WHERE id = :id")
    suspend fun activatePattern(id: Long)

    @Delete
    suspend fun deletePattern(pattern: ShiftPatternEntity)

    @Query("DELETE FROM shift_patterns WHERE id = :id")
    suspend fun deletePatternById(id: Long)

    @Query("SELECT * FROM shift_patterns ORDER BY createdAt ASC")
    suspend fun getAllPatternsSync(): List<ShiftPatternEntity>
}
