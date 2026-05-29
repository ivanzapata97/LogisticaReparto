package com.example.logisticareparto.features.clients.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.logisticareparto.data.models.Client
import com.example.logisticareparto.features.clients.viewmodel.ClientsUiState
import com.example.logisticareparto.features.clients.viewmodel.ClientsViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(clientId: String, viewModel: ClientsViewModel, onBack: () -> Unit, onEditClick: () -> Unit) {
    val uiState = viewModel.uiState
    val redColor = Color(0xFFE30613)

    val client = remember(uiState) {
        if (uiState is ClientsUiState.Success) {
            uiState.clients.find { it.id == clientId }
        } else null
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle del Cliente", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = redColor)
            )
        }
    ) { padding ->
        if (client == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Cliente no encontrado")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Tarjeta de Identificación Principal (Imagen con Nombre encima)
                Card(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Imagen de fondo
                        if (client.imagenUrl.isNotEmpty()) {
                            AsyncImage(
                                model = client.imagenUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Placeholder si no hay imagen
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(redColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = redColor,
                                    modifier = Modifier.size(80.dp)
                                )
                            }
                        }

                        // Banner de texto inferior
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                    )
                                )
                                .padding(top = 32.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Text(
                                text = client.cliente,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Información General
                Text(text = "Información General", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 12.dp))

                DetailInfoItem(icon = Icons.Default.LocationOn, label = "Dirección", value = client.direccion)
                DetailInfoItem(icon = Icons.Default.Phone, label = "Contacto", value = client.contacto.joinToString(", "))
                DetailInfoItem(icon = Icons.Default.Badge, label = "CUIL", value = client.cuil.joinToString(", "))
                DetailInfoItem(icon = Icons.Default.Route, label = "Reparto", value = "Zona del ${client.reparto}")
                DetailInfoItem(icon = Icons.Default.CalendarToday, label = "Días de Visita", value = client.dias.joinToString(", "))

                val (horarioTexto, estaAbierto) = client.getEstadoHorario()
                DetailInfoItem(
                    icon = Icons.Default.AccessTime,
                    label = "Estado de Atención",
                    value = horarioTexto,
                    valueColor = if (estaAbierto) Color(0xFF4CAF50) else Color(0xFFFF5252)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Mini Mapa
                if (client.latitud != 0.0 && client.longitud != 0.0) {
                    val location = LatLng(client.latitud, client.longitud)
                    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(location, 15f) }

                    Card(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState) {
                            Marker(state = rememberMarkerState(position = location), title = client.cliente)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botón de Editar
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = redColor)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configurar Cliente", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DetailInfoItem(icon: ImageVector, label: String, value: String, valueColor: Color = Color.White) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(8.dp), color = Color(0xFFF5F5F5)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            Text(text = value.ifEmpty { "No especificado" }, fontSize = 16.sp, color = valueColor, fontWeight = FontWeight.Medium)
        }
    }
}
