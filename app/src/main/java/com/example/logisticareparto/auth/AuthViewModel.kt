package com.example.logisticareparto.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel(){
    //Instancia de firebase auth
    private val auth: FirebaseAuth = Firebase.auth

    //El estado que se observara
    var uiState by mutableStateOf<AuthUiState>(AuthUiState.Idle)
        private set

    fun login(email: String, password: String){
        uiState = AuthUiState.Loading

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            uiState = AuthUiState.Success
                        } else{
                            uiState = AuthUiState.Error(task.exception?.message ?:"Error al ingresar")
                        }
                    }
            } catch (e: Exception){
                uiState = AuthUiState.Error("Error de conexion: ${e.message}")
            }

        }
    }

    fun resetState(){
        uiState = AuthUiState.Idle
    }
}