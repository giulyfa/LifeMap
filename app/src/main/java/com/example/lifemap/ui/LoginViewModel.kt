package com.example.lifemap.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemap.data.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).userDao()

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = LoginState.Error("Compila tutti i campi")
            return
        }

        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                val user = dao.getUserByEmail(email)
                when {
                    user == null -> _state.value = LoginState.Error("Email non registrata")
                    user.password != password -> _state.value = LoginState.Error("Password errata")
                    else -> {
                        dao.updateLastLogin(user.id, System.currentTimeMillis())
                        _state.value = LoginState.Success
                    }
                }
            } catch (e: Exception) {
                _state.value = LoginState.Error("Errore durante il login$e")
            }
        }
    }

    fun resetState() { _state.value = LoginState.Idle }
}