package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val date: String, // Format: YYYY-MM-DD
    val amount: Double,
    val category: String = "Personal",
    val notes: String = "",
    val billId: Long? = null,
    val tags: List<String> = emptyList()
)
