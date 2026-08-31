package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ProductivityRecordEntity
import com.example.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY isCompleted ASC, priority = 'HIGH' DESC, priority = 'MEDIUM' DESC, deadlineEpochMillis ASC")
    fun getTasksForDate(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date LIKE :monthPattern ORDER BY date ASC, isCompleted ASC")
    fun getTasksForMonth(monthPattern: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY date DESC, isCompleted ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date < :todayDate AND isCompleted = 0")
    suspend fun getUncompletedTasksBeforeDate(todayDate: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND date = :todayDate ORDER BY priority = 'HIGH' DESC, deadlineEpochMillis ASC")
    suspend fun getPendingTasksForDate(todayDate: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND deadlineEpochMillis > :fromTimestamp ORDER BY deadlineEpochMillis ASC LIMIT 5")
    suspend fun getUpcomingDeadlines(fromTimestamp: Long): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProductivity(record: ProductivityRecordEntity)

    @Query("SELECT * FROM productivity_history ORDER BY date DESC LIMIT :limit")
    fun getProductivityHistory(limit: Int = 30): Flow<List<ProductivityRecordEntity>>

    @Query("SELECT * FROM productivity_history WHERE date = :date LIMIT 1")
    suspend fun getProductivityForDate(date: String): ProductivityRecordEntity?

    @Query("SELECT * FROM tasks WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC, isCompleted ASC")
    fun getTasksForDateRange(startDate: String, endDate: String): Flow<List<TaskEntity>>

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()

    @Query("DELETE FROM productivity_history")
    suspend fun clearAllProductivityHistory()

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int
}
