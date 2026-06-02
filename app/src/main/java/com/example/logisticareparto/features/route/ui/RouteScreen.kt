package com.example.logisticareparto.features.route.ui

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logisticareparto.features.clients.ui.ClientItem
import com.example.logisticareparto.features.clients.viewmodel.ClientsViewModel
import com.example.logisticareparto.features.clients.viewmodel.RouteUiState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(viewModel: ClientsViewModel, onClientClick: (String) -> Unit) {
    val context = LocalContext.current
    val redColor = Color(0xFFE30613)
    val routeState = viewModel.routeUiState

    // Función para convertir Uri a Bitmap de forma segura
    fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            null
        }
    }

    // Launcher para Cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { viewModel.processRouteImage(it) }
    }

    // Launcher para Galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            uriToBitmap(it)?.let { bitmap ->
                viewModel.processRouteImage(bitmap)
            }
        }
    }

    // Launcher para pedir Permiso de Cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Armar Ruta Dinámica", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    if (routeState is RouteUiState.Success) {
                        IconButton(onClick = { viewModel.resetRouteState() }) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Nuevo Escaneo", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = redColor)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (routeState) {
                is RouteUiState.Idle -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Escanea tu hoja de Coca-Cola",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            "Detectaremos los clientes automáticamente",
                            fontSize = 14.sp,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Botón Cámara con petición de permiso
                        Button(
                            onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = redColor),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Abrir Cámara y Escanear")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botón Galería
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = redColor),
                            border = androidx.compose.foundation.BorderStroke(1.dp, redColor)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Seleccionar de Galería")
                        }
                    }
                }

                is RouteUiState.Processing -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = redColor)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Gemini analizando la planilla...", fontWeight = FontWeight.Bold)
                        Text("Buscando clientes en la base de datos", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                is RouteUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Ocurrió un error", fontWeight = FontWeight.Bold, color = redColor)
                        Text(routeState.message, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.resetRouteState() }) {
                            Text("Reintentar")
                        }
                    }
                }

                is RouteUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Mapa con todos los clientes detectados
                        val clients = routeState.clients
                        if (clients.isNotEmpty()) {
                            val firstClient = clients.first()
                            val initialPos = LatLng(firstClient.latitud, firstClient.longitud)
                            val cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(initialPos, 12f)
                            }
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .padding(16.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState
                                ) {
                                    clients.forEach { client ->
                                        if (client.latitud != 0.0) {
                                            Marker(
                                                state = rememberMarkerState(position = LatLng(client.latitud, client.longitud)),
                                                title = client.cliente,
                                                snippet = "Cód: ${client.codigoCliente}"
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Clientes Detectados (${clients.size})",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )

                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(clients) { client ->
                                ClientItem(client = client, onClick = { onClientClick(client.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}
