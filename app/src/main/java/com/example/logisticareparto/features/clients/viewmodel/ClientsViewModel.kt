package com.example.logisticareparto.features.clients.viewmodel

import android.content.Context
import android.graphics.Bitmap
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
import com.example.logisticareparto.data.models.DeliveryRoute
import com.example.logisticareparto.data.models.RouteStats
import com.example.logisticareparto.data.models.RouteStop
import com.example.logisticareparto.data.repository.ClientRepository
import com.example.logisticareparto.data.repository.RouteRepository
import com.example.logisticareparto.data.repository.UserPreferencesRepository
import com.example.logisticareparto.notifications.RouteNotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.vertexai.vertexAI
import com.google.firebase.vertexai.type.content
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed class ClientsUiState {
    object Loading : ClientsUiState()
    data class Success(val clients: List<Client>) : ClientsUiState()
    data class Error(val message: String) : ClientsUiState()
}

sealed class RouteUiState {
    object Idle : RouteUiState()
    object Processing : RouteUiState()
    data class Success(val clients: List<Client>) : RouteUiState()
    data class Error(val message: String) : RouteUiState()
}

sealed class RouteSaveUiState {
    object Idle : RouteSaveUiState()
    object Saving : RouteSaveUiState()
    data class Success(val routeId: String) : RouteSaveUiState()
    data class Error(val message: String) : RouteSaveUiState()
}

sealed class ActiveRouteUiState {
    object Idle : ActiveRouteUiState()
    object Loading : ActiveRouteUiState()
    data class Success(val route: DeliveryRoute) : ActiveRouteUiState()
    data class Error(val message: String) : ActiveRouteUiState()
}

class ClientsViewModel(
    private val repository: ClientRepository,
    private val routeRepository: RouteRepository,
    private val userPrefs: UserPreferencesRepository
) : ViewModel() {
    
    var uiState by mutableStateOf<ClientsUiState>(ClientsUiState.Loading)
        private set
    
    var routeUiState by mutableStateOf<RouteUiState>(RouteUiState.Idle)
        private set

    var routeSaveUiState by mutableStateOf<RouteSaveUiState>(RouteSaveUiState.Idle)
        private set

    var activeRouteUiState by mutableStateOf<ActiveRouteUiState>(ActiveRouteUiState.Idle)
        private set

    var selectedTruck by mutableIntStateOf(userPrefs.getSelectedTruck())
        private set

    var currentLanguage by mutableStateOf(userPrefs.getLanguage())
        private set

    var driverStats by mutableStateOf<RouteStats?>(null)
        private set

    fun loadDriverStats() {
        viewModelScope.launch {
            routeRepository.getDriverStats()
                .onSuccess { stats ->
                    driverStats = stats
                }
                .onFailure {
                    driverStats = null
                }
        }
    }

    fun setLanguage(langCode: String) {
        userPrefs.saveLanguage(langCode)
        currentLanguage = langCode
        //la interfaz reacciona si usamos appCompatDelegate en la actividad o via SideEfecct
    }

    private var notificationJob: Job? = null
    private val notifiedClientIds = mutableSetOf<String>()

    // 'routeDraft' es la hoja editable compartida entre buscar y ruta.
    var routeDraft by mutableStateOf<List<Client>>(emptyList())
        private set

    var routeDraftSource by mutableStateOf("manual")
        private set

    private val generativeModel = Firebase.vertexAI(location = "us-central1").generativeModel("gemini-2.5-flash")

    val hasActiveRoute: Boolean
        get() = activeRouteUiState is ActiveRouteUiState.Success

    fun processRouteImage(bitmap: Bitmap) {
        routeUiState = RouteUiState.Processing
        viewModelScope.launch {
            try {
                val inputContent = content {
                    image(bitmap)
                    text("Esta es una planilla de logistica llamada 'Guía de Fletero'. " +
                         "Extrae todos los números de la columna 'Cliente' (códigos de cliente). " +
                         "Devuélveme solo los números separados por comas, nada más.")
                }
                
                val response = generativeModel.generateContent(inputContent)
                val codesText = response.text ?: ""
                val extractedCodes = codesText.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                
                matchClientsWithCodes(extractedCodes)
            } catch (e: Exception) {
                routeUiState = RouteUiState.Error("Error al procesar imagen: ${e.message}")
            }
        }
    }

    private fun matchClientsWithCodes(codes: List<String>) {
        viewModelScope.launch {
            val allClients = if (uiState is ClientsUiState.Success) {
                (uiState as ClientsUiState.Success).clients
            } else {
                repository.getClients()
            }

            val truckClients = getFilteredClients(allClients)
            val matchedClients = truckClients.filter { client ->
                codes.contains(client.codigoCliente)
            }.ifEmpty {
                allClients.filter { client ->
                    codes.contains(client.codigoCliente)
                }
            }

            if (matchedClients.isEmpty()) {
                routeUiState = RouteUiState.Error("No encontramos clientes de la planilla en la base actual")
                return@launch
            }

            replaceRouteDraft(matchedClients, source = "escaneo")
            routeUiState = RouteUiState.Success(matchedClients)
        }
    }

    fun resetRouteState() {
        routeUiState = RouteUiState.Idle
        routeDraft = emptyList()
        routeDraftSource = "manual"
        routeSaveUiState = RouteSaveUiState.Idle
    }

    fun clearRouteSaveState() {
        routeSaveUiState = RouteSaveUiState.Idle
    }

    fun clearActiveRouteState() {
        activeRouteUiState = ActiveRouteUiState.Idle
        stopClosingTimeMonitor()
    }

    fun isClientInRoute(clientId: String): Boolean {
        return routeDraft.any { it.id == clientId }
    }

    fun addClientToRoute(client: Client) {
        if (hasActiveRoute) return
        if (isClientInRoute(client.id)) return

        routeDraft = routeDraft + client
        routeDraftSource = "manual"
        routeSaveUiState = RouteSaveUiState.Idle
    }

    fun replaceRouteDraft(clients: List<Client>, source: String) {
        routeDraft = clients.distinctBy { it.id }
        routeDraftSource = source
        routeSaveUiState = RouteSaveUiState.Idle
    }

    fun removeClientFromRoute(clientId: String) {
        routeDraft = routeDraft.filterNot { it.id == clientId }
        routeSaveUiState = RouteSaveUiState.Idle
        if (routeDraft.isEmpty()) {
            routeUiState = RouteUiState.Idle
        }
    }

    fun moveClientInRoute(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in routeDraft.indices || toIndex !in routeDraft.indices) return
        val updatedDraft = routeDraft.toMutableList()
        val item = updatedDraft.removeAt(fromIndex)
        updatedDraft.add(toIndex, item)
        routeDraft = updatedDraft
        routeSaveUiState = RouteSaveUiState.Idle
    }

    fun moveClientUp(clientId: String) {
        val index = routeDraft.indexOfFirst { it.id == clientId }
        if (index > 0) {
            val updatedDraft = routeDraft.toMutableList()
            val current = updatedDraft[index]
            updatedDraft[index] = updatedDraft[index - 1]
            updatedDraft[index - 1] = current
            routeDraft = updatedDraft
            routeSaveUiState = RouteSaveUiState.Idle
        }
    }

    fun moveClientDown(clientId: String) {
        val index = routeDraft.indexOfFirst { it.id == clientId }
        if (index >= 0 && index < routeDraft.lastIndex) {
            val updatedDraft = routeDraft.toMutableList()
            val current = updatedDraft[index]
            updatedDraft[index] = updatedDraft[index + 1]
            updatedDraft[index + 1] = current
            routeDraft = updatedDraft
            routeSaveUiState = RouteSaveUiState.Idle
        }
    }

    fun moveClientToFirst(clientId: String) {
        val index = routeDraft.indexOfFirst { it.id == clientId }
        if (index > 0) {
            val updatedDraft = routeDraft.toMutableList()
            val item = updatedDraft.removeAt(index)
            updatedDraft.add(0, item)
            routeDraft = updatedDraft
            routeSaveUiState = RouteSaveUiState.Idle
        }
    }

    fun moveClientToLast(clientId: String) {
        val index = routeDraft.indexOfFirst { it.id == clientId }
        if (index >= 0 && index < routeDraft.lastIndex) {
            val updatedDraft = routeDraft.toMutableList()
            val item = updatedDraft.removeAt(index)
            updatedDraft.add(item)
            routeDraft = updatedDraft
            routeSaveUiState = RouteSaveUiState.Idle
        }
    }

    fun moveRouteStopUp(index: Int) {
        if (index <= 0 || index >= routeDraft.size) return
        val updatedDraft = routeDraft.toMutableList()
        val current = updatedDraft[index]
        updatedDraft[index] = updatedDraft[index - 1]
        updatedDraft[index - 1] = current
        routeDraft = updatedDraft
        routeSaveUiState = RouteSaveUiState.Idle
    }

    fun moveRouteStopDown(index: Int) {
        if (index < 0 || index >= routeDraft.lastIndex) return
        val updatedDraft = routeDraft.toMutableList()
        val current = updatedDraft[index]
        updatedDraft[index] = updatedDraft[index + 1]
        updatedDraft[index + 1] = current
        routeDraft = updatedDraft
        routeSaveUiState = RouteSaveUiState.Idle
    }

    fun clearRouteDraft() {
        routeDraft = emptyList()
        routeDraftSource = "manual"
        routeUiState = RouteUiState.Idle
        routeSaveUiState = RouteSaveUiState.Idle
    }

    fun startCurrentRoute(onRouteStarted: ((Int) -> Unit)? = null) {
        routeSaveUiState = RouteSaveUiState.Saving

        viewModelScope.launch {
            routeRepository.startRoute(selectedTruck, routeDraft)
                .onSuccess { routeId ->
                    loadActiveRoute()
                    routeDraft = emptyList()
                    routeDraftSource = "manual"
                    routeUiState = RouteUiState.Idle
                    routeSaveUiState = RouteSaveUiState.Success(routeId)
                    onRouteStarted?.invoke(selectedTruck)
                }
                .onFailure { error ->
                    routeSaveUiState = RouteSaveUiState.Error(error.message ?: "No se pudo iniciar la ruta")
                }
        }
    }

    fun markStopVisited(stop: RouteStop, onNextClientReady: ((RouteStop, Int) -> Unit)? = null) {
        viewModelScope.launch {
            routeRepository.markStopVisited(selectedTruck, stop.clientId)
                .onSuccess { updatedRoute ->
                    activeRouteUiState = ActiveRouteUiState.Success(updatedRoute)

                    val nextStop = updatedRoute.sortedStops().firstOrNull { !it.isVisited }
                    if (nextStop != null && nextStop.clientId != stop.clientId) {
                        onNextClientReady?.invoke(nextStop, selectedTruck)
                    }
                }
                .onFailure { error ->
                    routeSaveUiState = RouteSaveUiState.Error(error.message ?: "No se pudo marcar la visita")
                }
        }
    }

    fun finishActiveRoute() {
        viewModelScope.launch {
            routeRepository.finishRoute(selectedTruck)
                .onSuccess {
                    activeRouteUiState = ActiveRouteUiState.Idle
                    routeSaveUiState = RouteSaveUiState.Idle
                    routeDraft = emptyList()
                    routeUiState = RouteUiState.Idle
                    stopClosingTimeMonitor()
                }
                .onFailure { error ->
                    routeSaveUiState = RouteSaveUiState.Error(error.message ?: "No se pudo finalizar la ruta")
                }
        }
    }

    fun loadActiveRoute() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            activeRouteUiState = ActiveRouteUiState.Idle
            return
        }

        if (selectedTruck <= 0) {
            activeRouteUiState = ActiveRouteUiState.Idle
            return
        }

        activeRouteUiState = ActiveRouteUiState.Loading
        viewModelScope.launch {
            routeRepository.getActiveRouteForToday(selectedTruck)
                .onSuccess { route ->
                    if (route == null) {
                        activeRouteUiState = ActiveRouteUiState.Idle
                        stopClosingTimeMonitor()
                    } else {
                        activeRouteUiState = ActiveRouteUiState.Success(route)
                        startClosingTimeMonitor()
                    }
                }
                .onFailure { error ->
                    activeRouteUiState = ActiveRouteUiState.Error(
                        error.message ?: "No se pudo cargar la ruta activa"
                    )
                    stopClosingTimeMonitor()
                }
        }
    }

    fun getActiveRouteStops(): List<RouteStop> {
        val route = (activeRouteUiState as? ActiveRouteUiState.Success)?.route ?: return emptyList()
        return route.sortedStops()
    }

    fun getRouteStopClient(stop: RouteStop): Client {
        val allClients = (uiState as? ClientsUiState.Success)?.clients.orEmpty()
        return stop.toClient(allClients)
    }

    private fun startClosingTimeMonitor() {
        if (notificationJob?.isActive == true) return
        
        notificationJob = viewModelScope.launch {
            while (true) {
                checkClosingTimes()
                delay(60000) // revisar cada minuto
            }
        }
    }

    private fun stopClosingTimeMonitor() {
        notificationJob?.cancel()
        notificationJob = null
        notifiedClientIds.clear()
    }

    private fun checkClosingTimes() {
        val route = (activeRouteUiState as? ActiveRouteUiState.Success)?.route ?: return
        val allClients = (uiState as? ClientsUiState.Success)?.clients ?: return
        val now = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        route.stops.filter { !it.isVisited }.forEach { stop ->
            if (!notifiedClientIds.contains(stop.clientId)) {
                val client = allClients.find { it.id == stop.clientId }
                if (client != null && !client.es24 && client.cierre.isNotEmpty()) {
                    try {
                        val closeTime = LocalTime.parse(client.cierre, formatter)
                        val duration = Duration.between(now, closeTime)
                        val minutesToClose = duration.toMinutes()

                        if (minutesToClose in 1..30) {
                            notifiedClientIds.add(stop.clientId)
                            // la notificacion se disparara a traves de un callback o evento si fuera necesario,
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    init {
        if (FirebaseAuth.getInstance().currentUser != null) {
            fetchClients()
            if (selectedTruck > 0) {
                loadActiveRoute()
            }
        }
    }

    fun setTruck(truck: Int) {
        if (selectedTruck != 0 && selectedTruck != truck) {
            clearRouteDraft()
            clearActiveRouteState()
        }
        selectedTruck = truck
        userPrefs.saveSelectedTruck(truck)
        fetchClients()
        loadActiveRoute()
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
            try {
                repository.uploadImage(uri, BuildConfig.CLOUDINARY_UPLOAD_PRESET, BuildConfig.CLOUDINARY_CLOUD_NAME)
                    .onSuccess { imageUrl ->
                        updateClientImage(clientId, imageUrl)
                    }
                    .onFailure { error ->
                        uiState = ClientsUiState.Error("Error Cloudinary: ${error.message}")
                    }
            } catch (e: Exception) {
                uiState = ClientsUiState.Error("Error al subir imagen: ${e.message}")
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

    fun updateClientBasicData(clientId: String, codigo: String, nombre: String, direccion: String, reparto: Int) {
        viewModelScope.launch {
            repository.updateClientBasicData(clientId, mapOf(
                "codigoCliente" to codigo,
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
        if (FirebaseAuth.getInstance().currentUser == null) {
            uiState = ClientsUiState.Success(emptyList())
            return
        }

        if (uiState !is ClientsUiState.Success) {
            uiState = ClientsUiState.Loading
        }

        viewModelScope.launch {
            val clients = repository.getClients()
            uiState = ClientsUiState.Success(clients)
        }
    }
}
