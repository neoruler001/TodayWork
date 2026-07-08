package com.todaywork.app.data.db.dao

import androidx.room.*
import com.todaywork.app.data.db.entity.MemoEntity

@Dao
interface MemoDao {

    @Query("SELECT * FROM memos WHERE dateEpoch = :dateEpoch ORDER BY id ASC")
    suspend fun getMemosForDate(dateEpoch: Long): List<MemoEntity>

    @Query("SELECT * FROM memos WHERE dateEpoch BETWEEN :startEpoch AND :endEpoch ORDER BY dateEpoch ASC, id ASC")
    suspend fun getMemosForRange(startEpoch: Long, endEpoch: Long): List<MemoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemo(memo: MemoEntity): Long

    @Update
    suspend fun updateMemo(memo: MemoEntity)

    @Query("DELETE FROM memos WHERE id = :id")
    suspend fun deleteMemoById(id: Long)

    @Query("DELETE FROM memos WHERE dateEpoch = :dateEpoch")
    suspend fun deleteMemosForDate(dateEpoch: Long)
}
