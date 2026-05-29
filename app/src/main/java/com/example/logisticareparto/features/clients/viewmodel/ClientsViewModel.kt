package com.example.logisticareparto.features.clients.viewmodel

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
import com.example.logisticareparto.data.models.Client
import com.example.logisticareparto.data.repository.ClientRepository
import kotlinx.coroutines.launch
import java.util.Locale

sealed class ClientsUiState {
    object Loading : ClientsUiState()
    data class Success(val clients: List<Client>) : ClientsUiState()
    data class Error(val message: String) : ClientsUiState()
}

class ClientsViewModel(private val repository: ClientRepository) : ViewModel() {
    
    var uiState by mutableStateOf<ClientsUiState>(ClientsUiState.Loading)
        private set

    var selectedTruck by mutableIntStateOf(0)
        private set

    init {
        fetchClients()
    }

    fun setTruck(truck: Int) {
        selectedTruck = truck
        fetchClients()
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

    fun uploadImage(context: Context, uri: Uri, clientId: String) {
        viewModelScope.launch {
            repository.uploadImage(uri, BuildConfig.CLOUDINARY_UPLOAD_PRESET, BuildConfig.CLOUDINARY_CLOUD_NAME)
                .onSuccess { imageUrl ->
                    updateClientImage(clientId, imageUrl)
                }
                .onFailure { error ->
                    uiState = ClientsUiState.Error("Error Cloudinary: ${error.message}")
                }
        }
    }

    private fun updateClientImage(clientId: String, imageUrl: String) {
        viewModelScope.launch {
            repository.updateClientBasicData(clientId, mapOf("imagenUrl" to imageUrl))
                .onSuccess {
                    val clients = repository.getClients()
                    uiState = ClientsUiState.Success(clients)
                }
                .onFailure { error ->
                    uiState = ClientsUiState.Error("Error Firestore: ${error.message}")
                }
        }
    }

    fun updateClientBasicData(clientId: String, nombre: String, direccion: String, reparto: Int) {
        viewModelScope.launch {
            repository.updateClientBasicData(clientId, mapOf(
                "cliente" to nombre,
                "direccion" to direccion,
                "reparto" to reparto
            ))
            .onSuccess { fetchClients() }
            .onFailure { error ->
                uiState = ClientsUiState.Error("Error al actualizar datos: ${error.message}")
            }
        }
    }

    fun createClient(context: Context, client: Client, imageUri: Uri?) {
        viewModelScope.launch {
            val coords = getCoordinatesFromAddress(context, client.direccion)
            val clientWithCoords = if (coords != null) {
                client.copy(latitud = coords.first, longitud = coords.second)
            } else {
                client
            }

            if (imageUri != null) {
                repository.uploadImage(imageUri, BuildConfig.CLOUDINARY_UPLOAD_PRESET, BuildConfig.CLOUDINARY_CLOUD_NAME)
                    .onSuccess { imageUrl ->
                        saveClientToFirestore(clientWithCoords.copy(imagenUrl = imageUrl))
                    }
                    .onFailure { error ->
                        uiState = ClientsUiState.Error("Error al subir foto: ${error.message}")
                    }
            } else {
                saveClientToFirestore(clientWithCoords)
            }
        }
    }

    private fun getCoordinatesFromAddress(context: Context, address: String): Pair<Double, Double>? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
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
            repository.createClient(client)
                .onSuccess { fetchClients() }
                .onFailure { error ->
                    uiState = ClientsUiState.Error("Error al crear cliente: ${error.message}")
                }
        }
    }

    fun fetchClients() {
        if (uiState !is ClientsUiState.Success) {
            uiState = ClientsUiState.Loading
        }

        viewModelScope.launch {
            val clients = repository.getClients()
            uiState = ClientsUiState.Success(clients)
        }
    }
}
