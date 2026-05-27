package com.example.lifemap.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMemory(memory: Memory): Long

    @Query("SELECT * FROM memories WHERE userEmail = :email ORDER BY date DESC")
    fun getAllMemoriesForUser(email: String): Flow<List<Memory>>

    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getMemoryById(id: Int): Memory?

    @Update
    suspend fun updateMemory(memory: Memory)

    @Query("SELECT * FROM memories WHERE isFavorite = 1 AND userEmail = :email")
    suspend fun getAllFavoriteMemories(email: String): List<Memory>

    @Delete
    suspend fun delete(memory: Memory): Int
}
