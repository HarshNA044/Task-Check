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

    @Query("SELECT * FROM tasks WHERE userId = :userId AND date = :date ORDER BY isCompleted ASC, priority = 'HIGH' DESC, priority = 'MEDIUM' DESC, deadlineEpochMillis ASC")
    fun getTasksForDate(userId: String, date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND date LIKE :monthPattern ORDER BY date ASC, isCompleted ASC")
    fun getTasksForMonth(userId: String, monthPattern: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY date DESC, isCompleted ASC")
    fun getAllTasks(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND date < :todayDate AND isCompleted = 0")
    suspend fun getUncompletedTasksBeforeDate(userId: String, todayDate: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND isCompleted = 0 AND date = :todayDate ORDER BY priority = 'HIGH' DESC, deadlineEpochMillis ASC")
    suspend fun getPendingTasksForDate(userId: String, todayDate: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND isCompleted = 0 AND deadlineEpochMillis > :fromTimestamp ORDER BY deadlineEpochMillis ASC LIMIT 5")
    suspend fun getUpcomingDeadlines(userId: String, fromTimestamp: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

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

    @Query("SELECT * FROM productivity_history WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    fun getProductivityHistory(userId: String, limit: Int = 30): Flow<List<ProductivityRecordEntity>>

    @Query("SELECT * FROM productivity_history WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getProductivityForDate(userId: String, date: String): ProductivityRecordEntity?

    @Query("SELECT * FROM tasks WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date ASC, isCompleted ASC")
    fun getTasksForDateRange(userId: String, startDate: String, endDate: String): Flow<List<TaskEntity>>

    @Query("DELETE FROM tasks WHERE userId = :userId")
    suspend fun clearAllTasks(userId: String)

    @Query("DELETE FROM productivity_history WHERE userId = :userId")
    suspend fun clearAllProductivityHistory(userId: String)

    @Query("SELECT COUNT(*) FROM tasks WHERE userId = :userId")
    suspend fun getTaskCount(userId: String): Int
}
