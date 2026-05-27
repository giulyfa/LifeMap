package com.example.lifemap.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemap.data.AppDatabase
import com.example.lifemap.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _isAlreadyLogged =
        MutableStateFlow<Boolean?>(null)
    val isAlreadyLogged: StateFlow<Boolean?> = _isAlreadyLogged.asStateFlow()

    init {
        checkIfUserIsLogged()
    }

    private fun checkIfUserIsLogged() {
        viewModelScope.launch {
            try {
                val loggedUser = dao.getLoggedUser()
                _isAlreadyLogged.value = loggedUser != null
            } catch (_: Exception) {
                _isAlreadyLogged.value = false
            }
        }
    }

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
                        dao.updateLoginStatus(user.id, true)
                        _state.value = LoginState.Success
                    }
                }
            } catch (e: Exception) {
                _state.value = LoginState.Error("Errore durante il login $e")
            }
        }
    }

    fun loginWithGoogle(email: String, displayName: String?) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                val user = dao.getUserByEmail(email)
                val userId = if (user == null) {
                    val nome = displayName?.substringBefore(" ") ?: "Utente"
                    val cognome = displayName?.substringAfter(" ") ?: "Google"
                    val newUser = User(nome = nome, cognome = cognome, email = email, password = "GOOGLE_AUTH_PLACEHOLDER")
                    dao.insertUser(newUser).toInt()
                } else {
                    user.id
                }
                dao.updateLastLogin(userId, System.currentTimeMillis())
                dao.updateLoginStatus(userId, true)
                _state.value = LoginState.Success
            } catch (e: Exception) {
                _state.value = LoginState.Error("Errore durante la registrazione tramite Google $e")
            }
        }
    }

    fun loginWithBiometrics() {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                val user = dao.getMostRecentLoginUser()

                if (user != null) {
                    dao.updateLastLogin(user.id, System.currentTimeMillis())
                    dao.updateLoginStatus(user.id, true)
                    _state.value = LoginState.Success
                } else {
                    _state.value = LoginState.Error("Nessun utente recente. Accedi con email e password la prima volta.")
                }
            } catch (e: Exception) {
                _state.value = LoginState.Error("Errore durante l'accesso biometrico $e")
            }
        }
    }

    fun resetState() { _state.value = LoginState.Idle }
}
