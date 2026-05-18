package com.example.lifemap.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MemoryCategory {
    VIAGGI,
    CIBO,
    AMICI,
    SPORT,
    VARIE
}

@Entity(tableName = "memories")
data class Memory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val date: Long,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val category: MemoryCategory,
    val imagePath: String? = null,
    val isFavorite: Boolean = false
)