package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val paidAmount: Double = 0.0,
    val due: String, // Format: YYYY-MM-DD
    val isPaid: Boolean = false,
    val repeat: String = "monthly", // none, weekly, monthly, yearly
    val category: String = "Bills",
    val tags: List<String> = emptyList()
)
