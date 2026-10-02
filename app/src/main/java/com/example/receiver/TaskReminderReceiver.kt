package com.example.receiver

import android.content.Context
import android.content.Intent
import android.content.BroadcastReceiver
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

                if (reminderType == AlarmScheduler.TYPE_DEADLINE) {
                    val taskId = intent.getLongExtra(AlarmScheduler.EXTRA_TASK_ID, -1L)
                    val taskTitle = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_TITLE) ?: "Task Reminder"
                    val taskPriority = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_PRIORITY) ?: "MEDIUM"
                    val deadlineTime = intent.getLongExtra(AlarmScheduler.EXTRA_DEADLINE_TIME, System.currentTimeMillis())

                    // Verify if task still exists and is not already completed
                    val existingTask = if (taskId > 0) db.taskDao().getTaskById(taskId) else null
                    if (existingTask == null || !existingTask.isCompleted) {
                        NotificationHelper.showTaskDeadlineNotification(
                            context = context,
                            taskId = taskId,
                            taskTitle = existingTask?.title ?: taskTitle,
                            taskPriority = existingTask?.priority ?: taskPriority,
                            deadlineEpochMillis = deadlineTime
                        )
                    }
                    return@launch
                }

                val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                // Check all pending tasks on the local device
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
