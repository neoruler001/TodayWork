package com.todaywork.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.todaywork.app.data.db.entity.AlarmSettingEntity
import com.todaywork.app.data.model.ShiftType
import com.todaywork.app.worker.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * 특정 날짜 근무의 알람 설정
     * @param date 근무 날짜
     * @param shiftType 근무 타입
     * @param alarms 활성화된 알람 설정 목록
     */
    fun scheduleAlarms(
        date: LocalDate,
        shiftType: ShiftType,
        alarms: List<AlarmSettingEntity>
    ) {
        if (!shiftType.isWorkDay) return

        val startTime = LocalTime.of(shiftType.defaultStartHour, shiftType.defaultStartMin)
        val shiftStart = LocalDateTime.of(date, startTime)

        alarms.forEach { alarm ->
            if (!alarm.isEnabled) return@forEach
            // 특정 근무 타입 필터
            if (alarm.shiftTypeFilter.isNotBlank() && alarm.shiftTypeFilter != shiftType.name) return@forEach

            val alarmTime = shiftStart.minusMinutes(alarm.minutesBefore.toLong())
            val alarmEpoch = alarmTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            if (alarmEpoch <= System.currentTimeMillis()) return@forEach

            val requestCode = generateRequestCode(date, alarm.id)
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ACTION_SHIFT_ALARM
                putExtra(AlarmReceiver.EXTRA_SHIFT_TYPE, shiftType.label)
                putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, alarm.label)
                putExtra(AlarmReceiver.EXTRA_DATE, date.toEpochDay())
                putExtra(AlarmReceiver.EXTRA_MINUTES_BEFORE, alarm.minutesBefore)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmEpoch,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmEpoch,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                // 권한 없음 — 설정에서 알람 권한 안내 필요
                e.printStackTrace()
            }
        }
    }

    /**
     * 특정 날짜 알람 취소
     */
    fun cancelAlarms(date: LocalDate, alarmIds: List<Long>) {
        alarmIds.forEach { alarmId ->
            val requestCode = generateRequestCode(date, alarmId)
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }

    private fun generateRequestCode(date: LocalDate, alarmId: Long): Int {
        // 날짜 epoch + alarmId 조합으로 고유 코드 생성
        return (date.toEpochDay() * 100 + alarmId).toInt()
    }
}
