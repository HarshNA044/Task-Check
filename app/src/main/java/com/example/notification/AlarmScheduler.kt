package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.receiver.TaskReminderReceiver
import java.util.Calendar

object AlarmScheduler {

    const val EXTRA_REMINDER_TYPE = "extra_reminder_type"
    const val TYPE_MORNING = "morning"
    const val TYPE_EVENING = "evening"

    private const val REQUEST_CODE_MORNING = 2001
    private const val REQUEST_CODE_EVENING = 2002

    fun scheduleDailyReminders(
        context: Context,
        morningHour: Int = 9,
        morningMinute: Int = 0,
        eveningHour: Int = 18,
        eveningMinute: Int = 0
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        scheduleSingleAlarm(
            context = context,
            alarmManager = alarmManager,
            requestCode = REQUEST_CODE_MORNING,
            reminderType = TYPE_MORNING,
            targetHour = morningHour,
            targetMinute = morningMinute
        )

        scheduleSingleAlarm(
            context = context,
            alarmManager = alarmManager,
            requestCode = REQUEST_CODE_EVENING,
            reminderType = TYPE_EVENING,
            targetHour = eveningHour,
            targetMinute = eveningMinute
        )
    }

    private fun scheduleSingleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        reminderType: String,
        targetHour: Int,
        targetMinute: Int
    ) {
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_TYPE, reminderType)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val triggerTime = calendar.timeInMillis

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Inexact fallback if exact alarm permission is restricted
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun triggerTestReminder(context: Context) {
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_TYPE, "test")
        }
        context.sendBroadcast(intent)
    }
}
