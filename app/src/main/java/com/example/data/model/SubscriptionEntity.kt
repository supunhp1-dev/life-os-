package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val provider: String = "",
    val amount: Double,
    val cycle: String = "monthly", // weekly, monthly, quarterly, yearly
    val nextDue: String = "", // YYYY-MM-DD
    val status: String = "active", // active, paused, cancelled
    val cancelInfo: String = "",
    val notes: String = "",
    val tags: List<String> = emptyList()
)
