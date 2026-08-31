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

    fun getTasksForDate(userId: String, date: String): Flow<List<TaskEntity>> {
        return taskDao.getTasksForDate(userId, date)
    }

    fun getTasksForMonth(userId: String, yearMonthPrefix: String): Flow<List<TaskEntity>> {
        return taskDao.getTasksForMonth(userId, "$yearMonthPrefix%")
    }

    fun getAllTasks(userId: String): Flow<List<TaskEntity>> {
        return taskDao.getAllTasks(userId)
    }

    fun getProductivityHistory(userId: String, limit: Int = 30): Flow<List<ProductivityRecordEntity>> {
        return taskDao.getProductivityHistory(userId, limit)
    }

    suspend fun insertTask(task: TaskEntity): Long = withContext(Dispatchers.IO) {
        val id = taskDao.insertTask(task)
        updateProductivityForDate(task.userId, task.date)
        id
    }

    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task)
        updateProductivityForDate(task.userId, task.date)
    }

    suspend fun getTaskById(id: Long): TaskEntity? = withContext(Dispatchers.IO) {
        taskDao.getTaskById(id)
    }

    suspend fun toggleTaskCompletion(task: TaskEntity) = withContext(Dispatchers.IO) {
        val newCompleted = !task.isCompleted
        val updated = task.copy(
            isCompleted = newCompleted,
            completedAt = if (newCompleted) System.currentTimeMillis() else null
        )
        taskDao.updateTask(updated)
        updateProductivityForDate(task.userId, task.date)
    }

    suspend fun deleteTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.deleteTask(task)
        updateProductivityForDate(task.userId, task.date)
    }

    suspend fun deleteTaskById(userId: String, id: Long, date: String) = withContext(Dispatchers.IO) {
        taskDao.deleteTaskById(id)
        updateProductivityForDate(userId, date)
    }

    /**
     * Automatic Rollover: Checks for uncompleted tasks from dates prior to today
     * and rolls them over to today's date, incrementing their rollover count and
     * logging the historical penalty on past days.
     */
    suspend fun rolloverUncompletedTasks(userId: String, todayDate: String): Int = withContext(Dispatchers.IO) {
        val uncompletedPastTasks = taskDao.getUncompletedTasksBeforeDate(userId, todayDate)
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
            updateProductivityForDate(userId, pastDate)
        }
        // Update today's productivity score
        updateProductivityForDate(userId, todayDate)

        return@withContext uncompletedPastTasks.size
    }

    suspend fun getUpcomingDeadlines(userId: String): List<TaskEntity> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        taskDao.getUpcomingDeadlines(userId, now)
    }

    suspend fun getPendingTasksForDate(userId: String, date: String): List<TaskEntity> = withContext(Dispatchers.IO) {
        taskDao.getPendingTasksForDate(userId, date)
    }

    suspend fun updateProductivityForDate(userId: String, date: String) = withContext(Dispatchers.IO) {
        val tasks = taskDao.getTasksForDate(userId, date).firstOrNull() ?: emptyList()
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val uncompleted = total - completed

        val score = calculateScore(tasks)

        val record = ProductivityRecordEntity(
            userId = userId,
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

    fun getTasksForDateRange(userId: String, startDate: String, endDate: String): Flow<List<TaskEntity>> {
        return taskDao.getTasksForDateRange(userId, startDate, endDate)
    }

    suspend fun clearAllData(userId: String) = withContext(Dispatchers.IO) {
        taskDao.clearAllTasks(userId)
        taskDao.clearAllProductivityHistory(userId)
    }
}
