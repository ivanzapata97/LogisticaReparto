package com.example.logisticareparto.clients

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.example.logisticareparto.BuildConfig
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
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
    private var isCloudinaryInitialized = false
    
    var uiState by mutableStateOf<ClientsUiState>(ClientsUiState.Loading)
        private set

    init {
        fetchClients()
    }

    private fun initCloudinary(context: Context) {
        if (!isCloudinaryInitialized) {
            val config = mapOf(
                "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                "secure" to true
            )
            try {
                MediaManager.init(context, config)
            } catch (e: Exception) {
                // Ya inicializado o error
            }
            isCloudinaryInitialized = true
        }
    }

    fun uploadImage(context: Context, uri: Uri, clientId: String) {
        initCloudinary(context)
        
        MediaManager.get().upload(uri)
            .unsigned(BuildConfig.CLOUDINARY_UPLOAD_PRESET)
            .option("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["secure_url"] as String
                    updateClientImage(clientId, imageUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    uiState = ClientsUiState.Error("Error Cloudinary: ${error.description}")
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                }
            }).dispatch()
    }

    private fun updateClientImage(clientId: String, imageUrl: String) {
        viewModelScope.launch {
            try {
                db.collection("clientes").document(clientId)
                    .update("imagenUrl", imageUrl).await()
                
                // Refresco sin activar el loading
                val snapshot = db.collection("clientes").get().await()
                val clientList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Client::class.java)?.copy(id = doc.id)
                }
                uiState = ClientsUiState.Success(clientList)
            } catch (e: Exception) {
                uiState = ClientsUiState.Error("Error Firestore: ${e.message}")
            }
        }
    }

    fun updateClientBasicData(clientId: String, nombre: String, direccion: String) {
        viewModelScope.launch {
            try {
                db.collection("clientes").document(clientId)
                    .update(
                        mapOf(
                            "cliente" to nombre,
                            "direccion" to direccion
                        )
                    ).await()
                fetchClients()
            } catch (e: Exception) {
                uiState = ClientsUiState.Error("Error al actualizar datos: ${e.message}")
            }
        }
    }

    fun fetchClients() {
        // Solo mostramos Loading si no tenemos cargados previamente datos (primera carga)
        if (uiState !is ClientsUiState.Success) {
            uiState = ClientsUiState.Loading
        }

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
