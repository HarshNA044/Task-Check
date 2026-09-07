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
     * logging the negative productivity score on past days.
     */
    suspend fun rolloverUncompletedTasks(userId: String, todayDate: String): Int = withContext(Dispatchers.IO) {
        val uncompletedPastTasks = taskDao.getUncompletedTasksBeforeDate(userId, todayDate)
        if (uncompletedPastTasks.isEmpty()) return@withContext 0

        val today = LocalDate.parse(todayDate, dateFormatter)

        // 1. For each past date that had incompleted tasks, log a negative productivity score
        val tasksByPastDate = uncompletedPastTasks.groupBy { it.date }
        tasksByPastDate.forEach { (pastDate, uncompletedList) ->
            val completedList = taskDao.getCompletedTasksForDate(userId, pastDate)
            val completedCount = completedList.size
            val uncompletedCount = uncompletedList.size
            val totalCount = completedCount + uncompletedCount

            // Negative productivity score for incomplete tasks on past days
            val negativeScore = if (completedCount == 0) {
                -100
            } else {
                val uncompletedRatio = uncompletedCount.toDouble() / totalCount.toDouble()
                -((uncompletedRatio * 100.0).toInt()).coerceIn(-100, -10)
            }

            val record = ProductivityRecordEntity(
                userId = userId,
                date = pastDate,
                completedCount = completedCount,
                totalCount = totalCount,
                uncompletedCount = uncompletedCount,
                score = negativeScore
            )
            taskDao.insertOrUpdateProductivity(record)
        }

        // 2. Assign those incompleted tasks to the next day (today) and track exact delay in days
        uncompletedPastTasks.forEach { task ->
            val origDate = task.originalDate ?: task.date
            val daysDelayed = try {
                val orig = LocalDate.parse(origDate, dateFormatter)
                val diff = java.time.temporal.ChronoUnit.DAYS.between(orig, today).toInt()
                max(task.rolloverCount + 1, diff)
            } catch (e: Exception) {
                task.rolloverCount + 1
            }

            // Compute new deadline for today at the same time-of-day or end of day
            val originalZone = ZoneId.systemDefault()
            val originalTime = try {
                val origInstant = java.time.Instant.ofEpochMilli(task.deadlineEpochMillis)
                origInstant.atZone(originalZone).toLocalTime()
            } catch (e: Exception) {
                LocalTime.of(18, 0)
            }
            val candidateDeadline = today.atTime(originalTime).atZone(originalZone).toInstant().toEpochMilli()
            val newDeadlineEpoch = if (candidateDeadline > System.currentTimeMillis()) {
                candidateDeadline
            } else {
                today.atTime(23, 59, 59).atZone(originalZone).toInstant().toEpochMilli()
            }

            val rolledTask = task.copy(
                date = todayDate,
                deadlineEpochMillis = newDeadlineEpoch,
                originalDate = origDate,
                rolloverCount = daysDelayed
            )
            taskDao.updateTask(rolledTask)
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

        val todayStr = LocalDate.now().format(dateFormatter)
        val isPastDate = date < todayStr

        val score = if (isPastDate && uncompleted > 0) {
            if (completed == 0) -100 else -((uncompleted.toDouble() / total.toDouble()) * 100.0).toInt().coerceIn(-100, -10)
        } else {
            calculateScore(tasks)
        }

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
        val penaltyFactor = (penaltyPoints.toDouble() / (maxPossiblePoints * 2).toDouble()) * 30.0

        val finalScore = if (earnedPoints == 0 && penaltyPoints > 0) {
            -100
        } else {
            (completionRatioScore - penaltyFactor).toInt()
        }

        return finalScore.coerceIn(-100, 100)
    }

    fun getTasksForDateRange(userId: String, startDate: String, endDate: String): Flow<List<TaskEntity>> {
        return taskDao.getTasksForDateRange(userId, startDate, endDate)
    }

    suspend fun clearAllData(userId: String) = withContext(Dispatchers.IO) {
        taskDao.clearAllTasks(userId)
        taskDao.clearAllProductivityHistory(userId)
    }
}
