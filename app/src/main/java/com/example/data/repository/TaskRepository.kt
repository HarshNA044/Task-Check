package com.example.data.repository

import com.example.data.local.TaskDao
import com.example.data.model.ProductivityRecordEntity
import com.example.data.model.ProductivitySummary
import com.example.data.model.TaskEntity
import com.example.data.model.TaskPriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

class TaskRepository(private val taskDao: TaskDao) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getTasksForDate(date: String): Flow<List<TaskEntity>> {
        return taskDao.getTasksForDate(date)
    }

    fun getTasksForMonth(yearMonthPrefix: String): Flow<List<TaskEntity>> {
        return taskDao.getTasksForMonth("$yearMonthPrefix%")
    }

    fun getAllTasks(): Flow<List<TaskEntity>> {
        return taskDao.getAllTasks()
    }

    fun getProductivityHistory(limit: Int = 30): Flow<List<ProductivityRecordEntity>> {
        return taskDao.getProductivityHistory(limit)
    }

    suspend fun insertTask(task: TaskEntity): Long = withContext(Dispatchers.IO) {
        val id = taskDao.insertTask(task)
        updateProductivityForDate(task.date)
        id
    }

    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task)
        updateProductivityForDate(task.date)
    }

    suspend fun toggleTaskCompletion(task: TaskEntity) = withContext(Dispatchers.IO) {
        val newCompleted = !task.isCompleted
        val updated = task.copy(
            isCompleted = newCompleted,
            completedAt = if (newCompleted) System.currentTimeMillis() else null
        )
        taskDao.updateTask(updated)
        updateProductivityForDate(task.date)
    }

    suspend fun deleteTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.deleteTask(task)
        updateProductivityForDate(task.date)
    }

    suspend fun deleteTaskById(id: Long, date: String) = withContext(Dispatchers.IO) {
        taskDao.deleteTaskById(id)
        updateProductivityForDate(date)
    }

    /**
     * Automatic Rollover: Checks for uncompleted tasks from dates prior to today
     * and rolls them over to today's date, incrementing their rollover count and
     * logging the historical penalty on past days.
     */
    suspend fun rolloverUncompletedTasks(todayDate: String): Int = withContext(Dispatchers.IO) {
        val uncompletedPastTasks = taskDao.getUncompletedTasksBeforeDate(todayDate)
        if (uncompletedPastTasks.isEmpty()) return@withContext 0

        val affectedPastDates = mutableSetOf<String>()
        val today = LocalDate.parse(todayDate, dateFormatter)

        uncompletedPastTasks.forEach { task ->
            affectedPastDates.add(task.date)
            // Compute new deadline for today at the same time-of-day or 6 PM default
            val originalZone = ZoneId.systemDefault()
            val originalTime = try {
                val origInstant = java.time.Instant.ofEpochMilli(task.deadlineEpochMillis)
                origInstant.atZone(originalZone).toLocalTime()
            } catch (e: Exception) {
                LocalTime.of(18, 0)
            }
            val newDeadlineEpoch = today.atTime(originalTime).atZone(originalZone).toInstant().toEpochMilli()

            val rolledTask = task.copy(
                date = todayDate,
                deadlineEpochMillis = max(newDeadlineEpoch, System.currentTimeMillis()),
                originalDate = task.originalDate ?: task.date,
                rolloverCount = task.rolloverCount + 1
            )
            taskDao.updateTask(rolledTask)
        }

        // Update past days productivity scores (which penalized them for missed uncompleted tasks)
        affectedPastDates.forEach { pastDate ->
            updateProductivityForDate(pastDate)
        }
        // Update today's productivity score
        updateProductivityForDate(todayDate)

        return@withContext uncompletedPastTasks.size
    }

    suspend fun getUpcomingDeadlines(): List<TaskEntity> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        taskDao.getUpcomingDeadlines(now)
    }

    suspend fun getPendingTasksForDate(date: String): List<TaskEntity> = withContext(Dispatchers.IO) {
        taskDao.getPendingTasksForDate(date)
    }

    suspend fun updateProductivityForDate(date: String) = withContext(Dispatchers.IO) {
        val tasks = taskDao.getTasksForDate(date).firstOrNull() ?: emptyList()
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val uncompleted = total - completed

        val score = calculateScore(tasks)

        val record = ProductivityRecordEntity(
            date = date,
            completedCount = completed,
            totalCount = total,
            uncompletedCount = uncompleted,
            score = score
        )
        taskDao.insertOrUpdateProductivity(record)
    }

    fun calculateScore(tasks: List<TaskEntity>): Int {
        if (tasks.isEmpty()) return 100 // Clean slate

        var earnedPoints = 0
        var maxPossiblePoints = 0
        var penaltyPoints = 0

        tasks.forEach { task ->
            val priority = TaskPriority.fromString(task.priority)
            val weight = priority.weight
            maxPossiblePoints += weight

            if (task.isCompleted) {
                earnedPoints += weight
            } else {
                // Uncompleted task penalty: increases if rolled over
                val rolloverMultiplier = (task.rolloverCount + 1).coerceAtMost(3)
                penaltyPoints += priority.penaltyWeight * rolloverMultiplier
            }
        }

        if (maxPossiblePoints == 0) return 100

        val completionRatioScore = (earnedPoints.toDouble() / maxPossiblePoints.toDouble()) * 100.0
        // Penalty impact reduces the raw completion score
        val penaltyFactor = (penaltyPoints.toDouble() / (maxPossiblePoints * 2).toDouble()) * 30.0
        val finalScore = (completionRatioScore - penaltyFactor).toInt()

        return finalScore.coerceIn(0, 100)
    }

    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val count = taskDao.getTaskCount()
        if (count > 0) return@withContext

        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()

        // Create a few realistic tasks for today and surrounding days
        val seedTasks = mutableListOf<TaskEntity>()

        // Yesterday tasks (some completed, showing historical score)
        val yesterday = today.minusDays(1)
        val yesterdayStr = yesterday.format(dateFormatter)
        seedTasks.add(
            TaskEntity(
                title = "Review monthly project roadmap",
                description = "Outline key milestones and deliverables",
                date = yesterdayStr,
                deadlineEpochMillis = yesterday.atTime(17, 0).atZone(zone).toInstant().toEpochMilli(),
                priority = TaskPriority.HIGH.name,
                isCompleted = true,
                completedAt = yesterday.atTime(16, 30).atZone(zone).toInstant().toEpochMilli()
            )
        )
        seedTasks.add(
            TaskEntity(
                title = "Inbox zero & team check-in",
                description = "Clear unread emails and catch up on Slack",
                date = yesterdayStr,
                deadlineEpochMillis = yesterday.atTime(11, 0).atZone(zone).toInstant().toEpochMilli(),
                priority = TaskPriority.LOW.name,
                isCompleted = true,
                completedAt = yesterday.atTime(10, 45).atZone(zone).toInstant().toEpochMilli()
            )
        )

        // Today tasks
        val todayStr = today.format(dateFormatter)
        seedTasks.add(
            TaskEntity(
                title = "Design calendar dashboard presentation",
                description = "Prepare slide deck with progress charts and task summaries",
                date = todayStr,
                deadlineEpochMillis = today.atTime(15, 0).atZone(zone).toInstant().toEpochMilli(),
                priority = TaskPriority.HIGH.name,
                isCompleted = false
            )
        )
        seedTasks.add(
            TaskEntity(
                title = "Send sprint deliverables report",
                description = "Summarize completed tasks and velocity",
                date = todayStr,
                deadlineEpochMillis = today.atTime(18, 0).atZone(zone).toInstant().toEpochMilli(),
                priority = TaskPriority.MEDIUM.name,
                isCompleted = false
            )
        )
        seedTasks.add(
            TaskEntity(
                title = "30-minute fitness & stretch break",
                description = "Maintain energy and mental clarity",
                date = todayStr,
                deadlineEpochMillis = today.atTime(19, 30).atZone(zone).toInstant().toEpochMilli(),
                priority = TaskPriority.LOW.name,
                isCompleted = true,
                completedAt = today.atTime(12, 15).atZone(zone).toInstant().toEpochMilli()
            )
        )

        // Tomorrow tasks
        val tomorrow = today.plusDays(1)
        val tomorrowStr = tomorrow.format(dateFormatter)
        seedTasks.add(
            TaskEntity(
                title = "Quarterly budget review",
                description = "Audit upcoming expenses and allocate funds",
                date = tomorrowStr,
                deadlineEpochMillis = tomorrow.atTime(14, 0).atZone(zone).toInstant().toEpochMilli(),
                priority = TaskPriority.HIGH.name,
                isCompleted = false
            )
        )

        taskDao.insertTasks(seedTasks)

        // Seed some history entries for graphs
        for (i in 6 downTo 1) {
            val pastD = today.minusDays(i.toLong())
            val pStr = pastD.format(dateFormatter)
            val comp = (2..5).random()
            val uncomp = if (i == 2) 1 else 0
            val total = comp + uncomp
            val sc = if (uncomp == 0) 100 else 75
            taskDao.insertOrUpdateProductivity(
                ProductivityRecordEntity(
                    date = pStr,
                    completedCount = comp,
                    totalCount = total,
                    uncompletedCount = uncomp,
                    score = sc
                )
            )
        }

        updateProductivityForDate(todayStr)
    }
}
