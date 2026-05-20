package com.example.lifemap.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lifemap.data.Memory
import com.example.lifemap.data.MemoryCategory
import com.example.lifemap.data.MemoryDao
import com.example.lifemap.data.UserDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    val imagePath: String? = null,
    val isFavorite: Boolean = false
)

class MemoryViewModel(
    private val memoryDao: MemoryDao,
    private val userDao: UserDao
) : ViewModel() {

    private val _currentUserEmail = MutableStateFlow("")
    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = userDao.getMostRecentLoginUser()
            if (user != null) {
                _currentUserEmail.value = user.email
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val allMemories: StateFlow<List<Memory>> = _currentUserEmail
        .flatMapLatest { email ->
            if (email.isBlank()) flowOf(emptyList())
            else memoryDao.getAllMemoriesForUser(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun loadActiveUser() {
        viewModelScope.launch {
            val user = userDao.getMostRecentLoginUser()
            if (user != null) {
                _currentUserEmail.value = user.email
                android.util.Log.d("LifeMapDebug", "Utente caricato con successo: ${user.email}")
            }
        }
    }

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

    fun saveMemory() {
        val currentState = _uiState.value
        val email = _currentUserEmail.value

        // Non salva se il titolo è vuoto
        if (currentState.title.isBlank()) return

        if (currentState.title.isNotBlank() && email.isNotBlank()) {
            viewModelScope.launch {
                val newMemory = Memory(
                    title = currentState.title,
                    description = currentState.description,
                    latitude = currentState.latitude,
                    longitude = currentState.longitude,
                    address = currentState.address,
                    date = System.currentTimeMillis(),
                    category = currentState.category,
                    isFavorite = currentState.isFavorite,
                    userEmail = email
                )
                memoryDao.insertMemory(newMemory)
                resetUiState()
            }
        }
    }

    private fun resetUiState() {
        _uiState.value = MemoryUiState()
    }

    fun updateFavorite(isFavorite: Boolean) {
        _uiState.update { it.copy(isFavorite = isFavorite) }
    }

    fun toggleFavorite(memory: Memory) {
        viewModelScope.launch {
            val updatedMemory = memory.copy(isFavorite = !memory.isFavorite)
            memoryDao.updateMemory(updatedMemory)
        }
    }

    suspend fun getMemoryById(id: Int): Memory? {
        return memoryDao.getMemoryById(id)
    }


    class Factory(
        private val memoryDao: MemoryDao,
        private val userDao: UserDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MemoryViewModel::class.java)) {
                return MemoryViewModel(memoryDao, userDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
