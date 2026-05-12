package com.example.lifemap.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    // Inserisce un nuovo ricordo. Se l'ID esiste già, lo sostituisce.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: Memory)

    // Legge tutti i ricordi.
    // Flow per far sì che la UI si aggiorni appena viene aggiunto un nuovo ricordo
    @Query("SELECT * FROM memories ORDER BY date DESC")
    fun getAllMemories(): Flow<List<Memory>>

    // Cancella un ricordo specifico
    @Delete
    suspend fun deleteMemory(memory: Memory)

    // Cerca un ricordo per ID
    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getMemoryById(id: Int): Memory?

    // Filtra i ricordi per categoria
    @Query("SELECT * FROM memories WHERE category = :category ORDER BY date DESC")
    fun getMemoriesByCategory(category: MemoryCategory): Flow<List<Memory>>
}