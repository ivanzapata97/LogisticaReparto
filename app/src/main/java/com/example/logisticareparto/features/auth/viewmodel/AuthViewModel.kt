package com.example.logisticareparto.features.auth.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logisticareparto.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    var uiState by mutableStateOf<AuthUiState>(AuthUiState.Idle)
        private set

    fun login(email: String, password: String) {
        uiState = AuthUiState.Loading
        viewModelScope.launch {
            repository.signIn(email, password)
                .onSuccess { uiState = AuthUiState.Success }
                .onFailure { error -> 
                    uiState = AuthUiState.Error(error.message ?: "Error al ingresar")
                }
        }
    }

    fun signUp(email: String, password: String) {
        uiState = AuthUiState.Loading
        viewModelScope.launch {
            repository.signUp(email, password)
                .onSuccess { uiState = AuthUiState.Success }
                .onFailure { error ->
                    uiState = AuthUiState.Error(error.message ?: "Error al registrarse")
                }
        }
    }

    fun logout() {
        repository.signOut()
        uiState = AuthUiState.Idle
    }

    fun resetState() {
        uiState = AuthUiState.Idle
    }
}
