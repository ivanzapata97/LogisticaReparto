package com.example.logisticareparto.clients

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class ClientsUiState {
    object Loading : ClientsUiState()
    data class Success(val clients: List<Client>) : ClientsUiState()
    data class Error(val message: String) : ClientsUiState()
}

class ClientsViewModel : ViewModel() {
    private val db = Firebase.firestore
    
    var uiState by mutableStateOf<ClientsUiState>(ClientsUiState.Loading)
        private set

    init {
        fetchClients()
    }

    fun fetchClients() {
        uiState = ClientsUiState.Loading
        viewModelScope.launch {
            try {
                val snapshot = db.collection("clientes").get().await()
                val clientList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Client::class.java)?.copy(id = doc.id)
                }
                uiState = ClientsUiState.Success(clientList)
            } catch (e: Exception) {
                uiState = ClientsUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
