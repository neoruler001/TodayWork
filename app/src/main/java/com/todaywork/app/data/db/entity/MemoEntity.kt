package com.todaywork.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.todaywork.app.data.model.MemoItem

@Entity(tableName = "memos")
data class MemoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpoch: Long,
    val title: String,
    val colorHex: Long = 0xFF26C6DAL,
    val startTimeMinutes: Int = -1,
    val endTimeMinutes: Int = -1,
    val isAllDay: Boolean = true
) {
    fun toMemoItem() = MemoItem(
        id = id,
        title = title,
        colorHex = colorHex,
        startTimeMinutes = startTimeMinutes,
        endTimeMinutes = endTimeMinutes,
        isAllDay = isAllDay
    )
}
