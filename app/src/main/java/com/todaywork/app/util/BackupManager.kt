package com.todaywork.app.util

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.todaywork.app.data.db.entity.AlarmSettingEntity
import com.todaywork.app.data.db.entity.MemoEntity
import com.todaywork.app.data.db.entity.ShiftPatternEntity
import com.todaywork.app.data.db.entity.WorkRecordEntity
import java.time.LocalDateTime

data class BackupData(
    val version: Int = 1,
    val exportedAt: String = LocalDateTime.now().toString(),
    val patterns: List<ShiftPatternEntity> = emptyList(),
    val workRecords: List<WorkRecordEntity> = emptyList(),
    val memos: List<MemoEntity> = emptyList(),
    val alarms: List<AlarmSettingEntity> = emptyList()
)

object BackupManager {

    fun write(context: Context, uri: Uri, data: BackupData, gson: Gson) {
        val json = gson.toJson(data)
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(json.toByteArray(Charsets.UTF_8))
        } ?: throw IllegalStateException("파일을 열 수 없습니다")
    }

    fun read(context: Context, uri: Uri, gson: Gson): BackupData {
        val json = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes().toString(Charsets.UTF_8)
        } ?: throw IllegalStateException("파일을 읽을 수 없습니다")
        return gson.fromJson(json, BackupData::class.java)
            ?: throw IllegalStateException("백업 파일 형식이 올바르지 않습니다")
    }

    fun suggestedFileName(): String {
        val now = LocalDateTime.now()
        return "TodayWork_backup_%d%02d%02d.json".format(now.year, now.monthValue, now.dayOfMonth)
    }
}
