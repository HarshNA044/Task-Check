package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object NotificationHelper {

    const val CHANNEL_ID = "task_daily_reminders_v2"
    const val CHANNEL_NAME = "Daily Task Reminders"

    const val CHANNEL_DEADLINE_ID = "task_deadline_notifications_v3"
    const val CHANNEL_DEADLINE_NAME = "Task Deadline Notifications"

    const val NOTIFICATION_ID_MORNING = 1001
    const val NOTIFICATION_ID_EVENING = 1002
    const val NOTIFICATION_ID_TEST = 1003

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            val dailyChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Twice-a-day reminders to help you complete upcoming tasks before their deadline"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 200, 250)
                setSound(soundUri, audioAttributes)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val deadlineChannel = NotificationChannel(
                CHANNEL_DEADLINE_ID,
                CHANNEL_DEADLINE_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications delivered when a task deadline is reached"
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(dailyChannel)
            notificationManager.createNotificationChannel(deadlineChannel)
        }
    }

    fun showTaskDeadlineNotification(
        context: Context,
        taskId: Long,
        taskTitle: String,
        taskPriority: String,
        deadlineEpochMillis: Long
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val notificationId = (50000 + (taskId % 10000)).toInt()

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val timeStr = try {
            val localTime = Instant.ofEpochMilli(deadlineEpochMillis).atZone(ZoneId.systemDefault()).toLocalTime()
            localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            "now"
        }

        val priorityBadge = when (taskPriority.uppercase()) {
            "HIGH" -> "High Priority"
            "LOW" -> "Low Priority"
            else -> "Medium Priority"
        }

        val title = "Task Deadline Reached: $taskTitle"
        val message = "Deadline for this task is reached."
        val bigText = "Deadline for this task is reached.\n\nTask: $taskTitle ($priorityBadge)\nScheduled: $timeStr"

        val largeLogoBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.app_user_custom_logo_1788276025844)
        } catch (e: Exception) {
            null
        }

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_DEADLINE_ID)
            .setSmallIcon(R.drawable.ic_notification_task_check)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setColor(0xFF2563EB.toInt())
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (largeLogoBitmap != null) {
            notificationBuilder.setLargeIcon(largeLogoBitmap)
        }

        val notification = notificationBuilder.build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    fun showTaskDeadlineAlarm(
        context: Context,
        taskId: Long,
        taskTitle: String,
        taskPriority: String,
        deadlineEpochMillis: Long
    ) {
        // Redirect to standard notification without alarm sound per user request
        showTaskDeadlineNotification(context, taskId, taskTitle, taskPriority, deadlineEpochMillis)
    }

    fun showTaskReminder(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        pendingTaskCount: Int,
        highPriorityTitle: String? = null,
        playChime: Boolean = true
    ) {
        createNotificationChannel(context)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val bigText = buildString {
            append(message)
            if (!highPriorityTitle.isNullOrBlank()) {
                append("\n🔥 Top Priority: ")
                append(highPriorityTitle)
            }
            if (pendingTaskCount > 0) {
                append("\n💡 Stay on track to preserve your 100% productivity score!")
            }
        }

        val largeLogoBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.app_user_custom_logo_1788276025844)
        } catch (e: Exception) {
            null
        }

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_task_check)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setColor(0xFF2563EB.toInt())
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 200, 250))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (largeLogoBitmap != null) {
            notificationBuilder.setLargeIcon(largeLogoBitmap)
        }

        val notification = notificationBuilder.build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)

        if (playChime) {
            try {
                val ringtone = RingtoneManager.getRingtone(context.applicationContext, soundUri)
                ringtone?.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
