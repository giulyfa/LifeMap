package com.example.lifemap.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lifemap.data.Memory
import com.example.lifemap.data.MemoryCategory
import com.example.lifemap.data.MemoryDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MemoryUiState(
    val title: String = "",
    val description: String = "",
    val category: MemoryCategory = MemoryCategory.VARIE,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val imagePath: String? = null
)

class MemoryViewModel(private val memoryDao: MemoryDao) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    val allMemories: StateFlow<List<Memory>> = memoryDao.getAllMemories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Funzioni per aggiornare l'interfaccia
    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun updateDescription(newDesc: String) {
        _uiState.update { it.copy(description = newDesc) }
    }

    fun updateLocation(lat: Double, lng: Double, address: String) {
        _uiState.update { it.copy(latitude = lat, longitude = lng, address = address) }
    }

    fun updateCategory(newCategory: MemoryCategory) {
        _uiState.update { it.copy(category = newCategory) }
    }

    // Funzione per salvare nel database
    fun saveMemory() {
        val currentState = _uiState.value

        // Non salva se il titolo è vuoto
        if (currentState.title.isBlank()) return

        val newMemory = Memory(
            title = currentState.title,
            description = currentState.description,
            date = System.currentTimeMillis(),
            latitude = currentState.latitude,
            longitude = currentState.longitude,
            address = currentState.address,
            category = currentState.category,
            imagePath = currentState.imagePath
        )

        viewModelScope.launch {
            memoryDao.insertMemory(newMemory)
            _uiState.value = MemoryUiState()
        }
    }

    suspend fun getMemoryById(id: Int): Memory? {
        return memoryDao.getMemoryById(id)
    }

    // Factory per creare il ViewModel
    class Factory(private val memoryDao: MemoryDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MemoryViewModel::class.java)) {
                return MemoryViewModel(memoryDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
