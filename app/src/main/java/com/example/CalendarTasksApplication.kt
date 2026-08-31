package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.TaskRepository
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CalendarTasksApplication : Application() {

    lateinit var repository: TaskRepository
        private set

    lateinit var themePreferences: com.example.data.local.ThemePreferences
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(this)
        repository = TaskRepository(database.taskDao())
        themePreferences = com.example.data.local.ThemePreferences(this)

        NotificationHelper.createNotificationChannel(this)
        AlarmScheduler.scheduleDailyReminders(
            context = this,
            morningHour = themePreferences.morningHour.value,
            morningMinute = themePreferences.morningMinute.value,
            eveningHour = themePreferences.eveningHour.value,
            eveningMinute = themePreferences.eveningMinute.value
        )

        CoroutineScope(Dispatchers.IO).launch {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            repository.rolloverUncompletedTasks(todayStr)
        }
    }
}
