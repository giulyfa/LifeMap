package com.example.lifemap.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lifemap.data.Memory
import com.example.lifemap.data.MemoryCategory
import com.example.lifemap.data.MemoryDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MemoryUiState(
    val title: String = "",
    val description: String = "",
    val category: MemoryCategory = MemoryCategory.ALTRO,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val imagePath: String? = null
)

class MemoryViewModel(private val memoryDao: MemoryDao) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    // Funzioni per aggiornare l'interfaccia (chiamate dai TextField del BottomSheet)
    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun updateDescription(newDesc: String) {
        _uiState.update { it.copy(description = newDesc) }
    }

    fun updateLocation(lat: Double, lng: Double, address: String) {
        _uiState.update { it.copy(latitude = lat, longitude = lng, address = address) }
    }

    // Funzione per salvare definitivamente nel database
    fun saveMemory() {
        val currentState = _uiState.value

        // Evitiamo di salvare se il titolo è vuoto
        if (currentState.title.isBlank()) return

        val newMemory = Memory(
            title = currentState.title,
            description = currentState.description,
            date = System.currentTimeMillis(), // Salva la data attuale
            latitude = currentState.latitude,
            longitude = currentState.longitude,
            address = currentState.address,
            category = currentState.category,
            imagePath = currentState.imagePath
        )

        // Lanciamo una coroutine per scrivere nel database senza bloccare l'interfaccia
        viewModelScope.launch {
            memoryDao.insertMemory(newMemory)

            // Dopo aver salvato, resettiamo lo stato così il BottomSheet è pulito per la prossima volta
            _uiState.value = MemoryUiState()
        }
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