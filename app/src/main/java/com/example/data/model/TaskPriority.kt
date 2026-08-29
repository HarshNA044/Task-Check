package com.example.data.model

enum class TaskPriority(
    val title: String,
    val weight: Int,
    val penaltyWeight: Int
) {
    HIGH(title = "High", weight = 30, penaltyWeight = 25),
    MEDIUM(title = "Medium", weight = 20, penaltyWeight = 15),
    LOW(title = "Low", weight = 10, penaltyWeight = 8);

    companion object {
        fun fromString(value: String): TaskPriority {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}
