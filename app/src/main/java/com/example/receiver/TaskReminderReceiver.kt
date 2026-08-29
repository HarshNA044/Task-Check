package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val reminderType = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_TYPE) ?: AlarmScheduler.TYPE_MORNING

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val pendingTasks = db.taskDao().getPendingTasksForDate(todayStr)
                val topTask = pendingTasks.firstOrNull()

                val notificationId = when (reminderType) {
                    AlarmScheduler.TYPE_MORNING -> NotificationHelper.NOTIFICATION_ID_MORNING
                    AlarmScheduler.TYPE_EVENING -> NotificationHelper.NOTIFICATION_ID_EVENING
                    else -> NotificationHelper.NOTIFICATION_ID_TEST
                }

                val title = when (reminderType) {
                    AlarmScheduler.TYPE_MORNING -> "🌅 Morning Focus: ${pendingTasks.size} Tasks Today"
                    AlarmScheduler.TYPE_EVENING -> "🌙 Evening Check-in: Beat Your Deadlines!"
                    else -> "🔔 Task Reminder Test Notification"
                }

                val message = if (pendingTasks.isEmpty()) {
                    "You have no pending tasks today! Great job staying productive."
                } else {
                    "${pendingTasks.size} task(s) remaining today. Complete them before deadline to keep your productivity score high!"
                }

                NotificationHelper.showTaskReminder(
                    context = context,
                    notificationId = notificationId,
                    title = title,
                    message = message,
                    pendingTaskCount = pendingTasks.size,
                    highPriorityTitle = topTask?.title
                )

                // Reschedule for next day if morning or evening
                AlarmScheduler.scheduleDailyReminders(context)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
