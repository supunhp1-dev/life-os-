package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val date: String, // Format: YYYY-MM-DD
    val time: String = "", // Format: HH:mm (optional)
    val priority: String = "Medium", // High, Medium, Low
    val isDone: Boolean = false,
    val cost: Double = 0.0,
    val eventId: Long? = null,
    val notes: String = "",
    val tags: List<String> = emptyList()
)
