package com.example.lifemap.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemap.data.AppDatabase
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                userDao.logoutAllUsers()
                onSuccess()
            } catch (_: Exception) {
                onSuccess()
            }
        }
    }
}
