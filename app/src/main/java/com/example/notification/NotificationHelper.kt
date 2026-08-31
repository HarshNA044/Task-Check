package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object NotificationHelper {

    const val CHANNEL_ID = "task_daily_reminders_v2"
    const val CHANNEL_NAME = "Daily Task Reminders"

    const val CHANNEL_DEADLINE_ID = "task_deadline_alerts_v2"
    const val CHANNEL_DEADLINE_NAME = "Task Deadline Sound Alerts"

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

            val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val alarmAudioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val deadlineChannel = NotificationChannel(
                CHANNEL_DEADLINE_ID,
                CHANNEL_DEADLINE_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Sound and vibration alarms triggered at the exact time set for your task deadlines"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 250, 400, 250, 400)
                setSound(alarmSoundUri, alarmAudioAttributes)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(dailyChannel)
            notificationManager.createNotificationChannel(deadlineChannel)
        }
    }

    fun playTaskCreationSound(context: Context) {
        try {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, soundUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showTaskDeadlineAlarm(
        context: Context,
        taskId: Long,
        taskTitle: String,
        taskPriority: String,
        deadlineEpochMillis: Long
    ) {
        createNotificationChannel(context)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

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
            "HIGH" -> "🚨 HIGH PRIORITY"
            "LOW" -> "📝 Low Priority"
            else -> "⚡ Priority"
        }

        val title = "⏰ Task Deadline Reached: $taskTitle"
        val message = "Scheduled deadline at $timeStr is due now! [$priorityBadge]"
        val bigText = "$message\n\nOpen Chrono Focus to complete or update this task and protect your productivity score."

        val notification = NotificationCompat.Builder(context, CHANNEL_DEADLINE_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 400, 250, 400, 250, 400))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, false)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)

        // Play loud sound chime immediately
        try {
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, soundUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 200, 250))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

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
