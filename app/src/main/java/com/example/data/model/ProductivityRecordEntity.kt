package com.example.data.model

import androidx.room.Entity

@Entity(tableName = "productivity_history", primaryKeys = ["userId", "date"])
data class ProductivityRecordEntity(
    val userId: String = "harshna63@gmail.com",
    val date: String, // "YYYY-MM-DD"
    val completedCount: Int,
    val totalCount: Int,
    val uncompletedCount: Int,
    val score: Int // 0 - 100
)

data class ProductivitySummary(
    val todayScore: Int = 100,
    val streakDays: Int = 0,
    val completedToday: Int = 0,
    val totalToday: Int = 0,
    val pendingToday: Int = 0,
    val overduePenaltyTotal: Int = 0,
    val weeklyAverageScore: Int = 100,
    val weeklyHistory: List<ProductivityRecordEntity> = emptyList()
)

data class DayBadgeInfo(
    val date: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val hasHighPriority: Boolean,
    val hasRolledOver: Boolean
)
