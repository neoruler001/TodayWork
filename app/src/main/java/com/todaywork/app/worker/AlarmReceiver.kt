package com.todaywork.app.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.todaywork.app.MainActivity
import com.todaywork.app.R
import com.todaywork.app.TodayWorkApp
import com.todaywork.app.data.db.dao.AlarmSettingDao
import com.todaywork.app.data.db.dao.WorkRecordDao
import com.todaywork.app.data.model.ShiftType
import com.todaywork.app.util.AlarmScheduler
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 근무 알람 BroadcastReceiver
 * 부팅 완료 수신 + 알람 트리거 처리
 */
class AlarmReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AlarmReceiverEntryPoint {
        fun alarmScheduler(): AlarmScheduler
        fun alarmSettingDao(): AlarmSettingDao
        fun workRecordDao(): WorkRecordDao
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                val ep = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    AlarmReceiverEntryPoint::class.java
                )
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val enabled = ep.alarmSettingDao().getEnabledAlarms()
                        if (enabled.isNotEmpty()) {
                            val today = LocalDate.now()
                            for (i in 0L..30L) {
                                val date = today.plusDays(i)
                                val record = ep.workRecordDao().getRecordByDate(date.toEpochDay()) ?: continue
                                val shiftType = runCatching { ShiftType.valueOf(record.shiftTypeName) }.getOrNull() ?: continue
                                ep.alarmScheduler().scheduleAlarms(date, shiftType, enabled)
                            }
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            ACTION_SHIFT_ALARM -> {
                val shiftType   = intent.getStringExtra(EXTRA_SHIFT_TYPE) ?: return
                val alarmLabel  = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: return
                val minsBefore  = intent.getIntExtra(EXTRA_MINUTES_BEFORE, 0)

                showNotification(context, shiftType, alarmLabel, minsBefore)
            }
        }
    }

    private fun showNotification(
        context: Context,
        shiftType: String,
        alarmLabel: String,
        minutesBefore: Int
    ) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val tapPending = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "$shiftType 근무 알림"
        val body = when {
            minutesBefore >= 60 -> "${minutesBefore / 60}시간 후 $shiftType 근무 시작"
            minutesBefore > 0   -> "${minutesBefore}분 후 $shiftType 근무 시작"
            else                -> "$shiftType 근무가 시작됩니다"
        }

        val notification = NotificationCompat.Builder(context, TodayWorkApp.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setSubText(alarmLabel)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(tapPending)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val ACTION_SHIFT_ALARM   = "com.todaywork.app.SHIFT_ALARM"
        const val EXTRA_SHIFT_TYPE     = "shift_type"
        const val EXTRA_ALARM_LABEL    = "alarm_label"
        const val EXTRA_DATE           = "date_epoch"
        const val EXTRA_MINUTES_BEFORE = "minutes_before"
    }
}
