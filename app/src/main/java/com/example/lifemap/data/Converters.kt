package com.example.lifemap.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromCategory(category: MemoryCategory): String {
        return category.name
    }

    @TypeConverter
    fun toCategory(value: String): MemoryCategory {
        return MemoryCategory.valueOf(value)
    }
}