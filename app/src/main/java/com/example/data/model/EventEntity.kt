package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val date: String, // Format: YYYY-MM-DD
    val startTime: String, // Format: HH:mm
    val endTime: String, // Format: HH:mm
    val category: String, // Work, Personal, Health, Home, Study, Finance
    val description: String = "",
    val location: String = "",
    val plannedCost: Double = 0.0,
    val repeat: String = "none", // none, daily, weekly, monthly
    val isAllDay: Boolean = false,
    val tags: List<String> = emptyList()
)
