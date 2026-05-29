package com.example.logisticareparto.features.auth.viewmodel

import android.os.Message

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object  Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}