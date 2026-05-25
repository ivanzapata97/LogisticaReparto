package com.example.logisticareparto.clients

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logisticareparto.BuildConfig
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

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

    var selectedTruck by mutableIntStateOf(0)
        private set

    init {
        fetchClients()
    }

    fun setTruck(truck: Int) {
        selectedTruck = truck
        fetchClients() // Refrescamos para aplicar filtros si fuera necesario o simplemente para tener el estado listo
    }

    private fun getCurrentDaySpanish(): String {
        val days = mapOf(
            "MONDAY" to "Lunes",
            "TUESDAY" to "Martes",
            "WEDNESDAY" to "Miércoles",
            "THURSDAY" to "Jueves",
            "FRIDAY" to "Viernes",
            "SATURDAY" to "Sábado",
            "SUNDAY" to "Domingo"
        )
        val dayName = java.time.LocalDate.now().dayOfWeek.name
        return days[dayName] ?: ""
    }

    fun getFilteredClients(allClients: List<Client>, onlyTruck: Boolean = true, onlyToday: Boolean = true): List<Client> {
        val today = getCurrentDaySpanish()
        return allClients.filter { client ->
            val matchesTruck = if (onlyTruck) client.reparto == selectedTruck else true
            if (onlyToday) {
                matchesTruck && client.dias.any { it.equals(today, ignoreCase = true) }
            } else {
                matchesTruck
            }
        }
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
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["secure_url"] as String
                    updateClientImage(clientId, imageUrl)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    uiState = ClientsUiState.Error("Error Cloudinary: ${error.description}")
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    private fun updateClientImage(clientId: String, imageUrl: String) {
        viewModelScope.launch {
            try {
                db.collection("clientes").document(clientId)
                    .update("imagenUrl", imageUrl).await()
                
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

    fun updateClientBasicData(clientId: String, nombre: String, direccion: String, reparto: Int) {
        viewModelScope.launch {
            try {
                db.collection("clientes").document(clientId)
                    .update(
                        mapOf(
                            "cliente" to nombre,
                            "direccion" to direccion,
                            "reparto" to reparto
                        )
                    ).await()
                fetchClients()
            } catch (e: Exception) {
                uiState = ClientsUiState.Error("Error al actualizar datos: ${e.message}")
            }
        }
    }

    fun createClient(context: Context, client: Client, imageUri: Uri?) {
        viewModelScope.launch {
            // geocoding: con esta herramienta de google obtenemos la latitud y longitud para el mapa
            val coords = getCoordinatesFromAddress(context, client.direccion)
            val clientWithCoords = if (coords != null) {
                client.copy(latitud = coords.first, longitud = coords.second)
            } else {
                client
            }

            // manejamos imagen y guardamos
            if (imageUri != null) {
                initCloudinary(context)
                MediaManager.get().upload(imageUri)
                    .unsigned(BuildConfig.CLOUDINARY_UPLOAD_PRESET)
                    .option("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {}
                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val imageUrl = resultData["secure_url"] as String
                            saveClientToFirestore(clientWithCoords.copy(imagenUrl = imageUrl))
                        }
                        override fun onError(requestId: String, error: ErrorInfo) {
                            uiState = ClientsUiState.Error("Error al subir foto: ${error.description}")
                        }
                        override fun onReschedule(requestId: String, error: ErrorInfo) {}
                    }).dispatch()
            } else {
                saveClientToFirestore(clientWithCoords)
            }
        }
    }

    private fun getCoordinatesFromAddress(context: Context, address: String): Pair<Double, Double>? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            // Nota: getFromLocationName es una llamada bloqueante, por eso está dentro de viewModelScope.launch
            val addresses = geocoder.getFromLocationName(address, 1)
            if (addresses?.isNotEmpty() == true) {
                val location = addresses[0]
                Pair(location.latitude, location.longitude)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun saveClientToFirestore(client: Client) {
        viewModelScope.launch {
            try {
                val clientMap = mapOf(
                    "cliente" to client.cliente,
                    "direccion" to client.direccion,
                    "contacto" to client.contacto,
                    "cuil" to client.cuil,
                    "dias" to client.dias,
                    "apertura" to client.apertura,
                    "cierre" to client.cierre,
                    "es24" to client.es24,
                    "imagenUrl" to client.imagenUrl,
                    "latitud" to client.latitud,
                    "longitud" to client.longitud,
                    "reparto" to client.reparto
                )
                db.collection("clientes").add(clientMap).await()
                fetchClients()
            } catch (e: Exception) {
                uiState = ClientsUiState.Error("Error al crear cliente: ${e.message}")
            }
        }
    }

    fun fetchClients() {
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
