package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ThemePreferences
import com.example.data.model.AppThemeMode
import com.example.data.model.DayBadgeInfo
import com.example.data.model.ProductivityRecordEntity
import com.example.data.model.ProductivitySummary
import com.example.data.model.TaskEntity
import com.example.data.model.TaskPriority
import com.example.data.repository.TaskRepository
import com.example.notification.AlarmScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

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

    // Tasks for currently selected day
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
    val monthDayBadges: StateFlow<Map<String, DayBadgeInfo>> = monthTasks.flatMapLatest { tasks ->
        val map = tasks.groupBy { it.date }.mapValues { (date, dayTasks) ->
            DayBadgeInfo(
                date = date,
                totalTasks = dayTasks.size,
                completedTasks = dayTasks.count { it.isCompleted },
                hasHighPriority = dayTasks.any { it.priority.equals(TaskPriority.HIGH.name, ignoreCase = true) && !it.isCompleted },
                hasRolledOver = dayTasks.any { it.rolloverCount > 0 }
            )
        }
        kotlinx.coroutines.flow.flowOf(map)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Productivity History (past 30 days)
    val productivityHistory: StateFlow<List<ProductivityRecordEntity>> = repository.getProductivityHistory(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Productivity Summary for today
    val productivitySummary: StateFlow<ProductivitySummary> = combine(
        rawTasksForSelectedDate,
        productivityHistory
    ) { todayTasks, history ->
        val todayScore = repository.calculateScore(todayTasks)
        val completed = todayTasks.count { it.isCompleted }
        val total = todayTasks.size
        val pending = total - completed
        val overduePenalty = todayTasks.filter { !it.isCompleted && it.rolloverCount > 0 }
            .sumOf { TaskPriority.fromString(it.priority).penaltyWeight * it.rolloverCount }

        // Calculate streak (consecutive past days with score >= 75)
        var streak = 0
        val sortedHistory = history.sortedByDescending { it.date }
        for (item in sortedHistory) {
            if (item.score >= 70) {
                streak++
            } else {
                break
            }
        }

        val avgScore = if (history.isNotEmpty()) {
            history.take(7).map { it.score }.average().toInt()
        } else todayScore

        ProductivitySummary(
            todayScore = todayScore,
            streakDays = streak,
            completedToday = completed,
            totalToday = total,
            pendingToday = pending,
            overduePenaltyTotal = overduePenalty,
            weeklyAverageScore = avgScore,
            weeklyHistory = history.take(7).reversed()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductivitySummary())

    init {
        performDailyRollover()
    }

    fun performDailyRollover() {
        viewModelScope.launch {
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

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
        }
    }

    fun saveTask(
        title: String,
        description: String,
        date: LocalDate,
        deadlineEpoch: Long,
        priority: TaskPriority
    ) {
        viewModelScope.launch {
            val currentEditing = _editingTask.value
            val dateStr = date.format(dateFormatter)
            if (currentEditing != null) {
                val updated = currentEditing.copy(
                    title = title.trim(),
                    description = description.trim(),
                    date = dateStr,
                    deadlineEpochMillis = deadlineEpoch,
                    priority = priority.name
                )
                repository.updateTask(updated)
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
            closeAddEditDialog()
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun testReminderNotification(context: Context) {
        AlarmScheduler.triggerTestReminder(context)
    }

    fun updateReminderTimes(
        context: Context,
        morningHour: Int,
        morningMinute: Int,
        eveningHour: Int,
        eveningMinute: Int
    ) {
        AlarmScheduler.scheduleDailyReminders(
            context = context,
            morningHour = morningHour,
            morningMinute = morningMinute,
            eveningHour = eveningHour,
            eveningMinute = eveningMinute
        )
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
