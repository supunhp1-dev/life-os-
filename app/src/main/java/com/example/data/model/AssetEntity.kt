package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val type: String = "Electronics", // Electronics, Home appliance, Vehicle, Furniture, Equipment
    val purchaseDate: String = "",
    val price: Double? = null,
    val warrantyEnd: String = "",
    val serial: String = "",
    val condition: String = "Good", // New, Good, Fair, Needs repair
    val notes: String = "",
    val tags: List<String> = emptyList()
)
