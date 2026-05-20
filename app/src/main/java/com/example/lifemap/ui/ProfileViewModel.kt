package com.example.lifemap.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemap.data.AppDatabase
import com.example.lifemap.data.MemoryCategory
import com.example.lifemap.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class CategoryStat(
    val category: MemoryCategory,
    val count: Int,
    val percentage: Float
)

data class ProfileUiState(
    val user: User? = null,
    val totalMemories: Int = 0,
    val categoryStats: List<CategoryStat> = emptyList(),
    val isLoading: Boolean = true
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val userDao = db.userDao()
    private val memoryDao = db.memoryDao()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val user = userDao.getMostRecentLoginUser()

            if (user != null) {
                memoryDao.getAllMemoriesForUser(user.email).collectLatest { memories ->
                    val total = memories.size

                    val grouped = memories.groupBy { it.category }
                    val stats = grouped.map { (category, list) ->
                        CategoryStat(
                            category = category,
                            count = list.size,
                            percentage = if (total > 0) list.size.toFloat() / total.toFloat() else 0f
                        )
                    }.sortedByDescending { it.count }

                    _uiState.value = ProfileUiState(
                        user = user,
                        totalMemories = total,
                        categoryStats = stats,
                        isLoading = false
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
