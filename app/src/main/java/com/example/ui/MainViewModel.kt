package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ThemePreferences
import com.example.data.model.AppThemeMode
import com.example.data.model.DayBadgeInfo
import com.example.data.model.Holiday
import com.example.data.model.HolidayProvider
import com.example.data.model.ProductivityRecordEntity
import com.example.data.model.ProductivitySummary
import com.example.data.model.TaskEntity
import com.example.data.model.TaskPriority
import com.example.data.repository.TaskRepository
import com.example.notification.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val repository: TaskRepository,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val themeMode: StateFlow<AppThemeMode> = themePreferences.themeMode
    val morningHour: StateFlow<Int> = themePreferences.morningHour
    val morningMinute: StateFlow<Int> = themePreferences.morningMinute
    val eveningHour: StateFlow<Int> = themePreferences.eveningHour
    val eveningMinute: StateFlow<Int> = themePreferences.eveningMinute
    val soundEnabled: StateFlow<Boolean> = themePreferences.soundEnabled

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val selectedDateHoliday: StateFlow<Holiday?> = _selectedDate
        .map { date -> HolidayProvider.getHoliday(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HolidayProvider.getHoliday(LocalDate.now()))

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _selectedPriorityFilter = MutableStateFlow<TaskPriority?>(null)
    val selectedPriorityFilter: StateFlow<TaskPriority?> = _selectedPriorityFilter.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow("ALL") // ALL, PENDING, COMPLETED
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter.asStateFlow()

    private val _isAddEditDialogOpen = MutableStateFlow(false)
    val isAddEditDialogOpen: StateFlow<Boolean> = _isAddEditDialogOpen.asStateFlow()

    private val _editingTask = MutableStateFlow<TaskEntity?>(null)
    val editingTask: StateFlow<TaskEntity?> = _editingTask.asStateFlow()

    private val _isSettingsSheetOpen = MutableStateFlow(false)
    val isSettingsSheetOpen: StateFlow<Boolean> = _isSettingsSheetOpen.asStateFlow()

    private val _isGraphsExpanded = MutableStateFlow(false)
    val isGraphsExpanded: StateFlow<Boolean> = _isGraphsExpanded.asStateFlow()

    private val _rolloverNotificationCount = MutableStateFlow<Int?>(null)
    val rolloverNotificationCount: StateFlow<Int?> = _rolloverNotificationCount.asStateFlow()

    // Tasks for currently selected day stored on local device
    val rawTasksForSelectedDate: StateFlow<List<TaskEntity>> = _selectedDate
        .flatMapLatest { date ->
            repository.getTasksForDate(date.format(dateFormatter))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered tasks for UI
    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        rawTasksForSelectedDate,
        _selectedPriorityFilter,
        _selectedStatusFilter
    ) { tasks, priorityFilter, statusFilter ->
        tasks.filter { task ->
            val matchesPriority = priorityFilter == null || task.priority.equals(priorityFilter.name, ignoreCase = true)
            val matchesStatus = when (statusFilter) {
                "PENDING" -> !task.isCompleted
                "COMPLETED" -> task.isCompleted
                else -> true
            }
            matchesPriority && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All tasks for the visible month to populate calendar day badges
    val monthTasks: StateFlow<List<TaskEntity>> = _currentMonth
        .flatMapLatest { ym ->
            val monthPattern = ym.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            repository.getTasksForMonth(monthPattern)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Badges per day mapped by "yyyy-MM-dd"
    val monthDayBadges: StateFlow<Map<String, DayBadgeInfo>> = monthTasks.map { tasks ->
        tasks.groupBy { it.date }.mapValues { (date, dayTasks) ->
            DayBadgeInfo(
                date = date,
                totalTasks = dayTasks.size,
                completedTasks = dayTasks.count { it.isCompleted },
                hasHighPriority = dayTasks.any { it.priority.equals(TaskPriority.HIGH.name, ignoreCase = true) && !it.isCompleted },
                hasRolledOver = dayTasks.any { it.rolloverCount > 0 }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Past 7-day tasks for dynamic graph & stats
    private val past7DaysTasks: StateFlow<List<TaskEntity>> = _selectedDate.flatMapLatest {
        val today = LocalDate.now()
        val startDateStr = today.minusDays(6).format(dateFormatter)
        val endDateStr = today.format(dateFormatter)
        repository.getTasksForDateRange(startDateStr, endDateStr)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Database productivity history (contains logged negative scores for uncompleted rolled-over days)
    private val productivityDbHistory: StateFlow<List<ProductivityRecordEntity>> = _selectedDate.flatMapLatest {
        repository.getProductivityHistory(30)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Productivity History & 7-Day Stats dynamically calculated from local device data
    val productivitySummary: StateFlow<ProductivitySummary> = combine(
        rawTasksForSelectedDate,
        past7DaysTasks,
        productivityDbHistory
    ) { todayTasks, rangeTasks, dbHistory ->
        val today = LocalDate.now()
        val todayDateStr = today.format(dateFormatter)

        val todayScore = repository.calculateScore(todayTasks)
        val completed = todayTasks.count { it.isCompleted }
        val total = todayTasks.size
        val pending = total - completed
        val overduePenalty = todayTasks.filter { !it.isCompleted && it.rolloverCount > 0 }
            .sumOf { TaskPriority.fromString(it.priority).penaltyWeight * it.rolloverCount }

        // Compute 7-day record points from 6 days ago to today
        val tasksByDate = rangeTasks.groupBy { it.date }
        val weeklyRecords = (6 downTo 0).map { offset ->
            val d = today.minusDays(offset.toLong())
            val dStr = d.format(dateFormatter)

            if (dStr == todayDateStr) {
                ProductivityRecordEntity(
                    userId = "local_device",
                    date = dStr,
                    completedCount = completed,
                    totalCount = total,
                    uncompletedCount = pending,
                    score = todayScore
                )
            } else {
                val savedRecord = dbHistory.find { it.date == dStr }
                val dTasks = tasksByDate[dStr] ?: emptyList()
                val rolledFromThisDate = (todayTasks + rangeTasks).filter { it.originalDate == dStr }

                if (savedRecord != null) {
                    savedRecord
                } else if (dTasks.isNotEmpty() || rolledFromThisDate.isNotEmpty()) {
                    val allAssociatedTasks = (dTasks + rolledFromThisDate).distinctBy { it.id }
                    val dCompleted = allAssociatedTasks.count { it.isCompleted }
                    val dUncompleted = allAssociatedTasks.count { !it.isCompleted || it.rolloverCount > 0 }
                    val dTotal = allAssociatedTasks.size

                    val score = if (dUncompleted > 0) {
                        if (dCompleted == 0) -100 else -((dUncompleted.toDouble() / dTotal.toDouble()) * 100.0).toInt().coerceIn(-100, -10)
                    } else {
                        100
                    }
                    ProductivityRecordEntity(
                        userId = "local_device",
                        date = dStr,
                        completedCount = dCompleted,
                        totalCount = dTotal,
                        uncompletedCount = dUncompleted,
                        score = score
                    )
                } else {
                    ProductivityRecordEntity(
                        userId = "local_device",
                        date = dStr,
                        completedCount = 0,
                        totalCount = 0,
                        uncompletedCount = 0,
                        score = 100
                    )
                }
            }
        }

        // Streak: consecutive past days with score >= 70 (breaks if day is negative or uncompleted)
        var streak = 0
        for (offset in 1..6) {
            val rec = weeklyRecords.find { it.date == today.minusDays(offset.toLong()).format(dateFormatter) }
            if (rec != null && rec.score >= 70 && rec.score > 0) {
                streak++
            } else if (rec != null && (rec.totalCount > 0 || rec.score < 0)) {
                break
            }
        }

        val daysWithTasks = weeklyRecords.filter { it.totalCount > 0 || it.score < 0 }
        val avgScore = if (daysWithTasks.isNotEmpty()) {
            daysWithTasks.map { it.score }.average().toInt().coerceIn(-100, 100)
        } else todayScore

        ProductivitySummary(
            todayScore = todayScore,
            streakDays = streak,
            completedToday = completed,
            totalToday = total,
            pendingToday = pending,
            overduePenaltyTotal = overduePenalty,
            weeklyAverageScore = avgScore,
            weeklyHistory = weeklyRecords
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductivitySummary())

    val productivityHistory: StateFlow<List<ProductivityRecordEntity>> = productivitySummary
        .map { summary -> summary.weeklyHistory }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        performDailyRollover()
    }

    fun performDailyRollover() {
        viewModelScope.launch(Dispatchers.IO) {
            val todayStr = LocalDate.now().format(dateFormatter)
            val rolledCount = repository.rolloverUncompletedTasks(todayStr)
            if (rolledCount > 0) {
                _rolloverNotificationCount.value = rolledCount
            }
        }
    }

    fun dismissRolloverBanner() {
        _rolloverNotificationCount.value = null
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        if (YearMonth.from(date) != _currentMonth.value) {
            _currentMonth.value = YearMonth.from(date)
        }
    }

    fun onMonthChanged(yearMonth: YearMonth) {
        _currentMonth.value = yearMonth
    }

    fun goToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
        _currentMonth.value = YearMonth.from(today)
    }

    fun setPriorityFilter(priority: TaskPriority?) {
        _selectedPriorityFilter.value = priority
    }

    fun setStatusFilter(status: String) {
        _selectedStatusFilter.value = status
    }

    fun openAddTaskDialog() {
        _editingTask.value = null
        _isAddEditDialogOpen.value = true
    }

    fun openEditTaskDialog(task: TaskEntity) {
        _editingTask.value = task
        _isAddEditDialogOpen.value = true
    }

    fun closeAddEditDialog() {
        _editingTask.value = null
        _isAddEditDialogOpen.value = false
    }

    fun openSettingsSheet() {
        _isSettingsSheetOpen.value = true
    }

    fun closeSettingsSheet() {
        _isSettingsSheetOpen.value = false
    }

    fun toggleGraphsExpanded() {
        _isGraphsExpanded.value = !_isGraphsExpanded.value
    }

    fun toggleTaskCompletion(context: Context, task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
            if (!task.isCompleted) {
                // Task is now completed, cancel deadline alarm
                AlarmScheduler.cancelTaskDeadlineAlarm(context, task.id)
            } else {
                // Task is marked uncompleted, reschedule if deadline is future
                if (task.deadlineEpochMillis > System.currentTimeMillis()) {
                    AlarmScheduler.scheduleTaskDeadlineAlarm(
                        context = context,
                        taskId = task.id,
                        taskTitle = task.title,
                        taskPriority = task.priority,
                        deadlineEpochMillis = task.deadlineEpochMillis
                    )
                }
            }
        }
    }

    fun saveTask(
        context: Context,
        title: String,
        description: String,
        date: LocalDate,
        deadlineEpoch: Long,
        priority: TaskPriority
    ) {
        viewModelScope.launch {
            val currentEditing = _editingTask.value
            val dateStr = date.format(dateFormatter)

            val taskId = if (currentEditing != null) {
                val updated = currentEditing.copy(
                    title = title.trim(),
                    description = description.trim(),
                    date = dateStr,
                    deadlineEpochMillis = deadlineEpoch,
                    priority = priority.name
                )
                repository.updateTask(updated)
                updated.id
            } else {
                val newTask = TaskEntity(
                    title = title.trim(),
                    description = description.trim(),
                    date = dateStr,
                    deadlineEpochMillis = deadlineEpoch,
                    priority = priority.name,
                    isCompleted = false
                )
                repository.insertTask(newTask)
            }

            // Schedule exact deadline sound reminder for task due time
            if (deadlineEpoch > System.currentTimeMillis()) {
                AlarmScheduler.scheduleTaskDeadlineAlarm(
                    context = context,
                    taskId = taskId,
                    taskTitle = title.trim(),
                    taskPriority = priority.name,
                    deadlineEpochMillis = deadlineEpoch
                )
            }

            closeAddEditDialog()
        }
    }

    fun deleteTask(context: Context, task: TaskEntity) {
        viewModelScope.launch {
            AlarmScheduler.cancelTaskDeadlineAlarm(context, task.id)
            repository.deleteTask(task)
        }
    }

    fun testReminderNotification(context: Context) {
        AlarmScheduler.triggerTestReminder(context)
    }

    fun setSoundEnabled(enabled: Boolean) {
        themePreferences.setSoundEnabled(enabled)
    }

    fun updateReminderTimes(
        context: Context,
        morningHour: Int,
        morningMinute: Int,
        eveningHour: Int,
        eveningMinute: Int
    ) {
        themePreferences.setReminderTimes(morningHour, morningMinute, eveningHour, eveningMinute)
        AlarmScheduler.scheduleDailyReminders(
            context = context,
            morningHour = morningHour,
            morningMinute = morningMinute,
            eveningHour = eveningHour,
            eveningMinute = eveningMinute
        )
    }

    fun clearAllTasks() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        themePreferences.setThemeMode(mode)
    }

    fun toggleThemeMode() {
        val nextMode = when (themeMode.value) {
            AppThemeMode.LIGHT -> AppThemeMode.DARK
            AppThemeMode.DARK -> AppThemeMode.LIGHT
            AppThemeMode.SYSTEM -> AppThemeMode.DARK
        }
        themePreferences.setThemeMode(nextMode)
    }

    class Factory(
        private val repository: TaskRepository,
        private val themePreferences: ThemePreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository, themePreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
