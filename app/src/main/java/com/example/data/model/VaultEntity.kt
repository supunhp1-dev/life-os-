package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault")
data class VaultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val type: String = "Warranty", // Warranty, Document, Insurance, Health, Asset
    val purchaseDate: String = "",
    val expiryDate: String = "", // Format: YYYY-MM-DD
    val reminderDays: Int = 30,
    val notes: String = "",
    val tags: List<String> = emptyList()
)
