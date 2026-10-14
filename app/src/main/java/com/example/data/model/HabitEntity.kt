package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val frequency: String = "daily",
    val streak: Int = 0,
    val lastLoggedDate: String = "",
    val notes: String = "",
    val tags: List<String> = emptyList()
)
