package com.example.lifemap.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemap.data.AppDatabase
import com.example.lifemap.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    object Success : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

class RegistrationViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).userDao()

    private val _state = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val state: StateFlow<RegistrationState> = _state

    fun register(nome: String, cognome: String, email: String, password: String, confirmPassword: String) {
        if (nome.isBlank() || cognome.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _state.value = RegistrationState.Error("Tutti i campi sono obbligatori")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.value = RegistrationState.Error("Email non valida")
            return
        }
        if (password.length < 6) {
            _state.value = RegistrationState.Error("La password deve avere almeno 6 caratteri")
            return
        }

        if (password != confirmPassword) {
            _state.value = RegistrationState.Error("Le password non coincidono")
            return
        }

        viewModelScope.launch {
            _state.value = RegistrationState.Loading
            try {
                val existing = dao.getUserByEmail(email)
                if (existing != null) {
                    _state.value = RegistrationState.Error("Email già registrata")
                    return@launch
                }
                dao.insertUser(User(nome = nome, cognome = cognome, email = email, password = password))
                _state.value = RegistrationState.Success
            } catch (e: Exception) {
                _state.value = RegistrationState.Error("Errore durante la registrazione$e")
            }
        }
    }

    fun resetState() { _state.value = RegistrationState.Idle }
}