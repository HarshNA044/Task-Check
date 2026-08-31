package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "harshna63@gmail.com", // Scoped per user account to isolate data
    val title: String,
    val description: String = "",
    val date: String, // "YYYY-MM-DD"
    val deadlineEpochMillis: Long, // timestamp for deadline
    val priority: String = TaskPriority.MEDIUM.name,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val originalDate: String? = null, // Set if rolled over from previous day
    val rolloverCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
