package com.example.lifemap.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    // Inserisce un nuovo ricordo e restituisce l'ID della riga.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: Memory): Long

    // Legge tutti i ricordi.
    @Query("SELECT * FROM memories WHERE userEmail = :email ORDER BY date DESC")
    fun getAllMemoriesForUser(email: String): Flow<List<Memory>>

    // Cancella un ricordo
    @Delete
    suspend fun deleteMemory(memory: Memory): Int

    // Cerca un ricordo per ID
    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getMemoryById(id: Int): Memory?

    // Filtra i ricordi per categoria
    @Query("SELECT * FROM memories WHERE category = :category ORDER BY date DESC")
    fun getMemoriesByCategory(category: MemoryCategory): Flow<List<Memory>>

    @Update
    suspend fun updateMemory(memory: Memory)

    @Query("SELECT * FROM memories WHERE isFavorite = 1 AND userEmail = :email")
    suspend fun getAllFavoriteMemories(email: String): List<Memory>
}
