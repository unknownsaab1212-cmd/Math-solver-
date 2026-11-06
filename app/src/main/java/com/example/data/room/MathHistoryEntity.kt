package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "math_history")
data class MathHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val question: String,
    val finalAnswer: String,
    val topic: String,
    val difficulty: String,
    val solutionJson: String,
    val isBookmarked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
