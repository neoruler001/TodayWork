package com.todaywork.app.data.repository

import com.todaywork.app.data.db.dao.SalarySettingDao
import com.todaywork.app.data.db.dao.WorkRecordDao
import com.todaywork.app.data.db.entity.SalarySettingEntity
import com.todaywork.app.data.model.ShiftType
import com.todaywork.app.data.model.WorkSummary
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class SalaryRepository @Inject constructor(
    private val salarySettingDao: SalarySettingDao,
    private val workRecordDao: WorkRecordDao
) {
    fun getSalarySettings(): Flow<SalarySettingEntity?> = salarySettingDao.getSalarySettings()

    suspend fun saveSalarySettings(setting: SalarySettingEntity) {
        salarySettingDao.upsert(setting)
    }

    /**
     * 특정 월 근무 요약 및 급여 계산
     */
    suspend fun calculateMonthlySummary(year: Int, month: Int): WorkSummary {
        val firstDay = LocalDate.of(year, month, 1)
        val lastDay  = firstDay.withDayOfMonth(firstDay.lengthOfMonth())
        val setting  = salarySettingDao.getSalarySettingsSync() ?: SalarySettingEntity()

        val records = workRecordDao.getRecordsBetweenSync(
            firstDay.toEpochDay(),
            lastDay.toEpochDay()
        )

        var totalWorkDays  = 0
        var totalWorkMins  = 0
        var regularMins    = 0
        var overtimeMins   = 0
        var nightMins      = 0
        var weekendMins    = 0
        var annualDays     = 0
        var halfDays       = 0

        val regularMinPerDay = setting.regularHoursPerDay * 60

        for (record in records) {
            val date = LocalDate.ofEpochDay(record.dateEpoch)
            val shiftType = runCatching { ShiftType.valueOf(record.shiftTypeName) }.getOrNull()
                ?: continue

            when (shiftType) {
                ShiftType.ANNUAL -> { annualDays++; continue }
                ShiftType.HALF_DAY_AM, ShiftType.HALF_DAY_PM -> { halfDays++; continue }
                else -> {}
            }
            if (!shiftType.isWorkDay) continue

            totalWorkDays++

            // 근무 시간 계산 (분 단위)
            val startMin = record.startTimeMinutes
            var endMin   = record.endTimeMinutes
            if (endMin <= startMin) endMin += 24 * 60  // 자정 넘기는 경우

            val workMins = endMin - startMin
            totalWorkMins += workMins

            // 정규/연장 분리
            val regularToday = minOf(workMins, regularMinPerDay)
            val overtimeToday = max(0, workMins - regularMinPerDay)
            regularMins  += regularToday
            overtimeMins += overtimeToday

            // 야간 근무 시간 계산 (22:00~06:00 = 1320~360)
            nightMins += calcNightMins(startMin, endMin)

            // 주말 여부
            val dow = date.dayOfWeek
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                weekendMins += workMins
            }
        }

        // 급여 계산
        val hourlyWage = setting.hourlyWage
        val baseSalary    = (regularMins / 60.0 * hourlyWage).toLong()
        val overtimePay   = (overtimeMins / 60.0 * hourlyWage * setting.overtimeRate).toLong()
        val nightPay      = (nightMins / 60.0 * hourlyWage * (setting.nightShiftRate - 1f)).toLong()
        val weekendPay    = (weekendMins / 60.0 * hourlyWage * (setting.weekendRate - 1f)).toLong()
        val mealAllowance = (totalWorkDays * setting.mealAllowancePerDay).toLong()

        return WorkSummary(
            year = year,
            month = month,
            totalWorkDays = totalWorkDays,
            totalWorkHours = totalWorkMins / 60.0,
            regularHours = regularMins / 60.0,
            overtimeHours = overtimeMins / 60.0,
            nightHours = nightMins / 60.0,
            weekendHours = weekendMins / 60.0,
            annualDaysUsed = annualDays,
            halfDaysUsed = halfDays,
            baseSalary = baseSalary,
            overtimePay = overtimePay,
            nightPay = nightPay,
            weekendPay = weekendPay,
            mealAllowance = mealAllowance,
            totalPay = baseSalary + overtimePay + nightPay + weekendPay + mealAllowance
        )
    }

    /**
     * 야간 근무 분 계산 (22:00~06:00 범위와의 교집합)
     * startMin, endMin: 자정 기준 분 (endMin > 1440 가능)
     */
    private fun calcNightMins(startMin: Int, endMin: Int): Int {
        // 야간 구간 1: 0~360 (00:00~06:00)
        // 야간 구간 2: 1320~1440 (22:00~24:00)
        // 야간 구간 3: 1440~1800 (익일 00:00~06:00, endMin이 1440 초과 시)
        val ranges = listOf(0 to 360, 1320 to 1440, 1440 to 1800)
        var nightMinutes = 0
        for ((ns, ne) in ranges) {
            val overlapStart = maxOf(startMin, ns)
            val overlapEnd   = minOf(endMin, ne)
            if (overlapEnd > overlapStart) nightMinutes += (overlapEnd - overlapStart)
        }
        return nightMinutes
    }
}
