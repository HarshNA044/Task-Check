package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.ThemePreferences
import com.example.data.repository.TaskRepository
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CalendarTasksApplication : Application() {

    val repository: TaskRepository by lazy {
        val database = AppDatabase.getDatabase(this)
        TaskRepository(database.taskDao())
    }

    val themePreferences: ThemePreferences by lazy {
        ThemePreferences(this)
    }

    override fun onCreate() {
        super.onCreate()

        // Asynchronously initialize notification channels, background alarms, and task rollovers
        // so the application cold start remains blazing fast, instant and non-blocking
        CoroutineScope(Dispatchers.IO).launch {
            try {
                NotificationHelper.createNotificationChannel(this@CalendarTasksApplication)
                AlarmScheduler.scheduleDailyReminders(
                    context = this@CalendarTasksApplication,
                    morningHour = themePreferences.morningHour.value,
                    morningMinute = themePreferences.morningMinute.value,
                    eveningHour = themePreferences.eveningHour.value,
                    eveningMinute = themePreferences.eveningMinute.value
                )
            } catch (e: Exception) {
                // Non-blocking initialization
            }

            try {
                val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                repository.rolloverUncompletedTasks(todayStr)
            } catch (e: Exception) {
                // Ignore background rollover errors
            }
        }
    }
}
