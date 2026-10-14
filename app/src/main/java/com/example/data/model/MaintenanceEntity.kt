package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maintenance")
data class MaintenanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val assetId: Long? = null,
    val type: String = "Routine service", // Routine service, Vehicle service, Safety inspection, Filter replacement, Repair
    val lastService: String = "",
    val nextDue: String = "",
    val intervalMonths: Int = 6,
    val estimatedCost: Double = 0.0,
    val provider: String = "",
    val notes: String = "",
    val tags: List<String> = emptyList()
)
