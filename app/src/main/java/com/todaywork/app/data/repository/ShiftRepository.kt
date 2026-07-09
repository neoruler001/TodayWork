package com.todaywork.app.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.todaywork.app.data.db.dao.AlarmSettingDao
import com.todaywork.app.data.db.dao.MemoDao
import com.todaywork.app.data.db.dao.ShiftPatternDao
import com.todaywork.app.data.db.dao.WorkRecordDao
import com.todaywork.app.data.db.entity.MemoEntity
import com.todaywork.app.data.db.entity.ShiftPatternEntity
import com.todaywork.app.data.db.entity.WorkRecordEntity
import com.todaywork.app.data.model.DayInfo
import com.todaywork.app.data.model.MemoItem
import com.todaywork.app.data.model.ShiftType
import com.todaywork.app.util.AlarmScheduler
import com.todaywork.app.util.HolidayUtil
import com.todaywork.app.util.LunarUtil
import com.todaywork.app.widget.CalendarWidget
import com.todaywork.app.worker.MemoAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShiftRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shiftPatternDao: ShiftPatternDao,
    private val workRecordDao: WorkRecordDao,
    private val alarmSettingDao: AlarmSettingDao,
    private val memoDao: MemoDao,
    private val alarmScheduler: AlarmScheduler,
    private val gson: Gson
) {
    private val holidayCache = mutableMapOf<Int, Map<LocalDate, HolidayUtil.Holiday>>()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // ── 패턴 관련 ─────────────────────────────────────────────

    fun getAllPatterns(): Flow<List<ShiftPatternEntity>> = shiftPatternDao.getAllPatterns()

    fun getActivePattern(): Flow<ShiftPatternEntity?> = shiftPatternDao.getActivePattern()

    suspend fun addPattern(
        name: String,
        cycles: List<ShiftType>,
        startDate: LocalDate,
        offsetDay: Int = 0
    ): Long {
        val cyclesJson = gson.toJson(cycles.map { it.name })
        val entity = ShiftPatternEntity(
            name = name,
            cyclesJson = cyclesJson,
            startDateEpoch = startDate.toEpochDay(),
            cycleOffsetDay = offsetDay
        )
        return shiftPatternDao.insertPattern(entity)
    }

    suspend fun activatePattern(patternId: Long) {
        shiftPatternDao.deactivateAll()
        shiftPatternDao.activatePattern(patternId)
        triggerWidgetUpdate(context)
    }

    suspend fun deletePattern(patternId: Long) {
        workRecordDao.deletePatternGeneratedRecords(patternId)
        shiftPatternDao.deletePatternById(patternId)
    }

    private suspend fun generateRecordsForPattern(patternId: Long) {
        val pattern = shiftPatternDao.getPatternById(patternId) ?: return
        val cycleType: List<String> = gson.fromJson(
            pattern.cyclesJson,
            object : TypeToken<List<String>>() {}.type
        )
        val today = LocalDate.now()
        val startDate = today.minusYears(1)
        val endDate = today.plusYears(2)
        val patternStartDate = LocalDate.ofEpochDay(pattern.startDateEpoch)
        val cycleLength = cycleType.size

        val records = mutableListOf<WorkRecordEntity>()
        var current = startDate
        while (!current.isAfter(endDate)) {
            val existing = workRecordDao.getRecordByDate(current.toEpochDay())
            if (existing != null && existing.isManual) {
                current = current.plusDays(1)
                continue
            }

            val daysDiff = (current.toEpochDay() - patternStartDate.toEpochDay()).toInt()
            val cycleIndex = ((daysDiff % cycleLength + cycleLength) % cycleLength +
                    pattern.cycleOffsetDay) % cycleLength
            val shiftTypeName = cycleType[cycleIndex]
            val shiftType = runCatching { ShiftType.valueOf(shiftTypeName) }.getOrNull()
                ?: ShiftType.REST

            records.add(
                WorkRecordEntity(
                    dateEpoch = current.toEpochDay(),
                    shiftTypeName = shiftType.name,
                    startTimeMinutes = shiftType.defaultStartHour * 60 + shiftType.defaultStartMin,
                    endTimeMinutes = shiftType.defaultEndHour * 60 + shiftType.defaultEndMin,
                    isManual = false,
                    patternId = patternId
                )
            )
            current = current.plusDays(1)
        }
        workRecordDao.deletePatternGeneratedRecords(patternId)
        workRecordDao.insertAll(records)
    }

    // ── 근무 기록 관련 ──────────────────────────────────────────

    suspend fun getDayInfosForMonth(year: Int, month: Int): List<DayInfo> {
        val firstDay = LocalDate.of(year, month, 1)
        val lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth())
        val holidays = getHolidays(year)

        val records = workRecordDao.getRecordsBetweenSync(
            firstDay.toEpochDay(),
            lastDay.toEpochDay()
        ).associateBy { it.dateEpoch }

        val memosByDate = memoDao.getMemosForRange(firstDay.toEpochDay(), lastDay.toEpochDay())
            .groupBy { it.dateEpoch }

        return (1..firstDay.lengthOfMonth()).map { day ->
            val date = LocalDate.of(year, month, day)
            val record = records[date.toEpochDay()]
            val shiftType = record?.let {
                runCatching { ShiftType.valueOf(it.shiftTypeName) }.getOrNull()
            }
            val lunar = LunarUtil.solarToLunar(date)
            val holiday = holidays[date]
            val memos = memosByDate[date.toEpochDay()]?.map { it.toMemoItem() } ?: emptyList()

            DayInfo(
                date = date,
                shiftType = shiftType,
                lunarDay = lunar?.toDisplayString() ?: "",
                isHoliday = holiday != null,
                holidayName = holiday?.name ?: "",
                memos = memos,
                startTime = record?.let { formatTime(it.startTimeMinutes) } ?: "",
                endTime = record?.let { formatTime(it.endTimeMinutes) } ?: "",
                isModified = record?.isManual ?: false
            )
        }
    }

    suspend fun updateWorkRecord(
        date: LocalDate,
        shiftType: ShiftType,
        startTimeMinutes: Int,
        endTimeMinutes: Int
    ) {
        val record = WorkRecordEntity(
            dateEpoch = date.toEpochDay(),
            shiftTypeName = shiftType.name,
            startTimeMinutes = startTimeMinutes,
            endTimeMinutes = endTimeMinutes,
            memo = "",
            isManual = true,
            patternId = -1L
        )
        workRecordDao.insertOrUpdate(record)
        scheduleAlarmsForDate(date, shiftType)
        triggerWidgetUpdate(context)
    }

    // ── 메모 관련 ──────────────────────────────────────────────

    suspend fun addMemo(
        date: LocalDate,
        title: String,
        colorHex: Long,
        startTimeMinutes: Int,
        endTimeMinutes: Int,
        isAllDay: Boolean,
        reminderMinutes: Int = -1
    ): Long {
        val entity = MemoEntity(
            dateEpoch = date.toEpochDay(),
            title = title,
            colorHex = colorHex,
            startTimeMinutes = startTimeMinutes,
            endTimeMinutes = endTimeMinutes,
            isAllDay = isAllDay,
            reminderMinutes = reminderMinutes
        )
        val id = memoDao.insertMemo(entity)
        if (reminderMinutes >= 0) scheduleMemoAlarm(id, date, startTimeMinutes, isAllDay, title, reminderMinutes)
        triggerWidgetUpdate(context)
        return id
    }

    suspend fun updateMemo(memo: MemoItem, date: LocalDate) {
        val entity = MemoEntity(
            id = memo.id,
            dateEpoch = date.toEpochDay(),
            title = memo.title,
            colorHex = memo.colorHex,
            startTimeMinutes = memo.startTimeMinutes,
            endTimeMinutes = memo.endTimeMinutes,
            isAllDay = memo.isAllDay,
            reminderMinutes = memo.reminderMinutes
        )
        memoDao.updateMemo(entity)
        cancelMemoAlarm(memo.id)
        if (memo.reminderMinutes >= 0) scheduleMemoAlarm(memo.id, date, memo.startTimeMinutes, memo.isAllDay, memo.title, memo.reminderMinutes)
        triggerWidgetUpdate(context)
    }

    suspend fun deleteMemo(id: Long) {
        cancelMemoAlarm(id)
        memoDao.deleteMemoById(id)
        triggerWidgetUpdate(context)
    }

    suspend fun rescheduleAllMemoAlarms() {
        val today = LocalDate.now()
        val farFuture = today.plusYears(2)
        val memos = memoDao.getMemosForRange(today.toEpochDay(), farFuture.toEpochDay())
        memos.filter { it.reminderMinutes >= 0 }.forEach { entity ->
            val date = LocalDate.ofEpochDay(entity.dateEpoch)
            scheduleMemoAlarm(entity.id, date, entity.startTimeMinutes, entity.isAllDay, entity.title, entity.reminderMinutes)
        }
    }

    private fun scheduleMemoAlarm(
        memoId: Long,
        date: LocalDate,
        startTimeMinutes: Int,
        isAllDay: Boolean,
        title: String,
        reminderMinutes: Int
    ) {
        val effectiveStartMinutes = if (isAllDay || startTimeMinutes < 0) 8 * 60 else startTimeMinutes
        val startTime = LocalTime.of(effectiveStartMinutes / 60, effectiveStartMinutes % 60)
        val alarmDateTime = LocalDateTime.of(date, startTime).minusMinutes(reminderMinutes.toLong())
        val alarmEpoch = alarmDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (alarmEpoch <= System.currentTimeMillis()) return

        val intent = Intent(context, MemoAlarmReceiver::class.java).apply {
            action = MemoAlarmReceiver.ACTION_MEMO_ALARM
            putExtra(MemoAlarmReceiver.EXTRA_MEMO_TITLE, title)
        }
        val requestCode = (memoId + 2_000_000L).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmEpoch, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmEpoch, pendingIntent)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun cancelMemoAlarm(memoId: Long) {
        val intent = Intent(context, MemoAlarmReceiver::class.java)
        val requestCode = (memoId + 2_000_000L).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(it)
        }
    }

    suspend fun getMemosForDate(date: LocalDate): List<MemoItem> =
        memoDao.getMemosForDate(date.toEpochDay()).map { it.toMemoItem() }

    // ── 패턴 복원 ──────────────────────────────────────────────

    suspend fun resetToPattern(date: LocalDate) {
        val pattern = shiftPatternDao.getActivePattern().first() ?: return
        val cycleType: List<String> = gson.fromJson(
            pattern.cyclesJson,
            object : TypeToken<List<String>>() {}.type
        )
        val patternStartDate = LocalDate.ofEpochDay(pattern.startDateEpoch)
        val cycleLength = cycleType.size
        val daysDiff = (date.toEpochDay() - patternStartDate.toEpochDay()).toInt()
        val cycleIndex = ((daysDiff % cycleLength + cycleLength) % cycleLength +
                pattern.cycleOffsetDay) % cycleLength
        val shiftType = runCatching { ShiftType.valueOf(cycleType[cycleIndex]) }.getOrElse { ShiftType.REST }

        val record = WorkRecordEntity(
            dateEpoch = date.toEpochDay(),
            shiftTypeName = shiftType.name,
            startTimeMinutes = shiftType.defaultStartHour * 60 + shiftType.defaultStartMin,
            endTimeMinutes = shiftType.defaultEndHour * 60 + shiftType.defaultEndMin,
            memo = "",
            isManual = false,
            patternId = pattern.id
        )
        workRecordDao.insertOrUpdate(record)
        val resetShiftType = runCatching { ShiftType.valueOf(record.shiftTypeName) }.getOrNull()
        if (resetShiftType != null) scheduleAlarmsForDate(date, resetShiftType)
        triggerWidgetUpdate(context)
    }

    suspend fun applyPatternToDateRange(patternId: Long, startDate: LocalDate, endDate: LocalDate) {
        val pattern = shiftPatternDao.getPatternById(patternId) ?: return
        val cycleType: List<String> = gson.fromJson(
            pattern.cyclesJson,
            object : TypeToken<List<String>>() {}.type
        )
        val cycleLength = cycleType.size
        val patternStartDate = LocalDate.ofEpochDay(pattern.startDateEpoch)

        // Clear all auto-generated records for this pattern so range-apply doesn't leave stale global records
        workRecordDao.deletePatternGeneratedRecords(patternId)

        var current = startDate
        while (!current.isAfter(endDate)) {
            val daysDiff = (current.toEpochDay() - patternStartDate.toEpochDay()).toInt()
            val cycleIndex = ((daysDiff % cycleLength + cycleLength) % cycleLength +
                    pattern.cycleOffsetDay) % cycleLength
            val shiftType = runCatching {
                ShiftType.valueOf(cycleType[cycleIndex])
            }.getOrElse { ShiftType.REST }

            workRecordDao.insertOrUpdate(WorkRecordEntity(
                dateEpoch = current.toEpochDay(),
                shiftTypeName = shiftType.name,
                startTimeMinutes = shiftType.defaultStartHour * 60 + shiftType.defaultStartMin,
                endTimeMinutes = shiftType.defaultEndHour * 60 + shiftType.defaultEndMin,
                memo = "",
                isManual = true,
                patternId = patternId
            ))
            current = current.plusDays(1)
        }
    }

    private fun getHolidays(year: Int): Map<LocalDate, HolidayUtil.Holiday> =
        holidayCache.getOrPut(year) { HolidayUtil.getHolidays(year) }

    private suspend fun scheduleAlarmsForDate(date: LocalDate, shiftType: ShiftType) {
        val enabled = alarmSettingDao.getEnabledAlarms()
        if (enabled.isEmpty()) return
        alarmScheduler.scheduleAlarms(date, shiftType, enabled)
    }

    private suspend fun scheduleAlarmsForUpcomingDays() {
        val enabled = alarmSettingDao.getEnabledAlarms()
        if (enabled.isEmpty()) return
        val today = LocalDate.now()
        for (i in 0L..30L) {
            val date = today.plusDays(i)
            val record = workRecordDao.getRecordByDate(date.toEpochDay()) ?: continue
            val shiftType = runCatching { ShiftType.valueOf(record.shiftTypeName) }.getOrNull() ?: continue
            alarmScheduler.scheduleAlarms(date, shiftType, enabled)
        }
    }

    private fun triggerWidgetUpdate(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(CalendarWidget::class.java)
            glanceIds.forEach { id ->
                CalendarWidget().update(context, id)
            }
        }
    }

    private fun formatTime(totalMinutes: Int): String {
        if (totalMinutes == 0) return ""
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return "%02d:%02d".format(h, m)
    }
}
