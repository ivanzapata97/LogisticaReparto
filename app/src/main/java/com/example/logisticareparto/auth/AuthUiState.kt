package com.example.logisticareparto.auth

import android.os.Message

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object  Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}