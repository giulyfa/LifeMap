package com.example.lifemap.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    // AGGIUNTO ": Long" ALLA FINE
    // Inserisce un nuovo ricordo e restituisce l'ID della riga.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: Memory): Long

    // Legge tutti i ricordi.
    @Query("SELECT * FROM memories ORDER BY date DESC")
    fun getAllMemories(): Flow<List<Memory>>

    // AGGIUNTO ": Int" ALLA FINE
    // Cancella un ricordo e restituisce il numero di righe modificate.
    @Delete
    suspend fun deleteMemory(memory: Memory): Int

    // Cerca un ricordo per ID (questo va già bene perché restituisce Memory?)
    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getMemoryById(id: Int): Memory?

    // Filtra i ricordi per categoria
    @Query("SELECT * FROM memories WHERE category = :category ORDER BY date DESC")
    fun getMemoriesByCategory(category: MemoryCategory): Flow<List<Memory>>
}