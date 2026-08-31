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
    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_TITLE = "extra_task_title"
    const val EXTRA_TASK_PRIORITY = "extra_task_priority"
    const val EXTRA_DEADLINE_TIME = "extra_deadline_time"

    const val TYPE_MORNING = "morning"
    const val TYPE_EVENING = "evening"
    const val TYPE_DEADLINE = "task_deadline"

    private const val REQUEST_CODE_MORNING = 2001
    private const val REQUEST_CODE_EVENING = 2002
    private const val BASE_REQUEST_CODE_DEADLINE = 30000

    fun scheduleTaskDeadlineAlarm(
        context: Context,
        taskId: Long,
        taskTitle: String,
        taskPriority: String,
        deadlineEpochMillis: Long
    ) {
        if (deadlineEpochMillis <= System.currentTimeMillis()) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val requestCode = (BASE_REQUEST_CODE_DEADLINE + (taskId % 50000)).toInt()

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_TYPE, TYPE_DEADLINE)
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, taskTitle)
            putExtra(EXTRA_TASK_PRIORITY, taskPriority)
            putExtra(EXTRA_DEADLINE_TIME, deadlineEpochMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    deadlineEpochMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    deadlineEpochMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                deadlineEpochMillis,
                pendingIntent
            )
        }
    }

    fun cancelTaskDeadlineAlarm(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val requestCode = (BASE_REQUEST_CODE_DEADLINE + (taskId % 50000)).toInt()
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

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
