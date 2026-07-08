package com.todaywork.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.todaywork.app.data.db.dao.AlarmSettingDao
import com.todaywork.app.data.db.dao.ShiftPatternDao
import com.todaywork.app.data.db.dao.WorkRecordDao
import com.todaywork.app.data.db.entity.ShiftPatternEntity
import com.todaywork.app.data.db.entity.WorkRecordEntity
import com.todaywork.app.data.model.DayInfo
import com.todaywork.app.data.model.ShiftType
import com.todaywork.app.util.AlarmScheduler
import com.todaywork.app.util.HolidayUtil
import com.todaywork.app.util.LunarUtil
import com.todaywork.app.widget.CalendarWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll

@Singleton
class ShiftRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shiftPatternDao: ShiftPatternDao,
    private val workRecordDao: WorkRecordDao,
    private val alarmSettingDao: AlarmSettingDao,
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
        generateRecordsForPattern(patternId)
        scheduleAlarmsForUpcomingDays()
        triggerWidgetUpdate()
    }

    suspend fun deletePattern(patternId: Long) {
        workRecordDao.deletePatternGeneratedRecords(patternId)
        shiftPatternDao.deletePatternById(patternId)
    }

    /**
     * 패턴 기반으로 ±2년 범위의 근무 기록을 미리 생성
     * isManual=false 기록만 생성 (수동 수정본은 유지)
     */
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
            // 이미 수동 수정된 날짜는 건너뜀
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

        return (1..firstDay.lengthOfMonth()).map { day ->
            val date = LocalDate.of(year, month, day)
            val record = records[date.toEpochDay()]
            val shiftType = record?.let {
                runCatching { ShiftType.valueOf(it.shiftTypeName) }.getOrNull()
            }
            val lunar = LunarUtil.solarToLunar(date)
            val holiday = holidays[date]

            DayInfo(
                date = date,
                shiftType = shiftType,
                lunarDay = lunar?.toDisplayString() ?: "",
                isHoliday = holiday != null,
                holidayName = holiday?.name ?: "",
                memo = record?.memo ?: "",
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
        endTimeMinutes: Int,
        memo: String
    ) {
        val record = WorkRecordEntity(
            dateEpoch = date.toEpochDay(),
            shiftTypeName = shiftType.name,
            startTimeMinutes = startTimeMinutes,
            endTimeMinutes = endTimeMinutes,
            memo = memo,
            isManual = true,
            patternId = -1L
        )
        workRecordDao.insertOrUpdate(record)
        scheduleAlarmsForDate(date, shiftType)
        triggerWidgetUpdate()
    }

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
        triggerWidgetUpdate()
    }

    private fun getHolidays(year: Int): Map<LocalDate, HolidayUtil.Holiday> {
        return holidayCache.getOrPut(year) { HolidayUtil.getHolidays(year) }
    }

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

   // ShiftRepository 내부
private suspend fun triggerWidgetUpdate(context: Context) {
    // GlanceAppWidgetManager를 통해 해당 위젯 클래스의 인스턴스들을 모두 업데이트
    GlanceAppWidgetManager(context).updateAppWidgetState(
        GlanceAppWidgetManager(context).getGlanceIds(CalendarWidget::class.java)
    ) { }
    CalendarWidget().updateAll(context) // 만약 이 코드를 꼭 써야 한다면 
    // 위와 같이 import가 제대로 되어 있는지 확인하거나, 아래 방식을 권장합니다.
    
    val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(CalendarWidget::class.java)
    glanceIds.forEach { id ->
        CalendarWidget().update(context, id)
    }
}

    private fun formatTime(totalMinutes: Int): String {
        if (totalMinutes == 0) return ""
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return "%02d:%02d".format(h, m)
    }

    suspend fun applyPatternToDateRange(patternId: Long, startDate: LocalDate, endDate: LocalDate) {
        val pattern = shiftPatternDao.getPatternById(patternId) ?: return
        val cycleType: List<String> = gson.fromJson(
            pattern.cyclesJson,
            object : TypeToken<List<String>>() {}.type
        )
        val cycleLength = cycleType.size
        
        var current = startDate
        var cycleIndex = 0
        
        while (!current.isAfter(endDate)) {
            val shiftTypeName = cycleType[cycleIndex % cycleLength]
            val shiftType = runCatching { ShiftType.valueOf(shiftTypeName) }.getOrElse { ShiftType.REST }
            
            // 기존 메모를 유지하려면 가져와야 하지만, 패턴 덮어쓰기이므로 초기화
            val record = WorkRecordEntity(
                dateEpoch = current.toEpochDay(),
                shiftTypeName = shiftType.name,
                startTimeMinutes = shiftType.defaultStartHour * 60 + shiftType.defaultStartMin,
                endTimeMinutes = shiftType.defaultEndHour * 60 + shiftType.defaultEndMin,
                memo = "",
                isManual = true,
                patternId = patternId
            )
            workRecordDao.insertOrUpdate(record)
            
            current = current.plusDays(1)
            cycleIndex++
        }
    }
}
