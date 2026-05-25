package com.example.logisticareparto.clients.edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.logisticareparto.clients.Client
import com.example.logisticareparto.clients.ClientsUiState
import com.example.logisticareparto.clients.ClientsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientEditScreen(clientId: String, viewModel: ClientsViewModel, onBack: () -> Unit, onSaveSuccess: () -> Unit) {
    val uiState = viewModel.uiState
    val redColor = Color(0xFFE30613)
    val context = LocalContext.current
    
    // Mantenemos el cliente en un estado local para que no desaparezca al cargar
    var currentClient by remember { mutableStateOf<Client?>(null) }

    LaunchedEffect(uiState) {
        if (uiState is ClientsUiState.Success) {
            val found = uiState.clients.find { it.id == clientId }
            if (found != null) currentClient = found
        }
    }

    // Estado local para los campos editables (se inicializan cuando se carga el cliente)
    var editedNombre by remember(currentClient?.id) { mutableStateOf(currentClient?.cliente ?: "") }
    var editedDireccion by remember(currentClient?.id) { mutableStateOf(currentClient?.direccion ?: "") }
    var editedReparto by remember(currentClient?.id) { mutableStateOf(currentClient?.reparto?.toString() ?: "") }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadImage(context.applicationContext, it, clientId) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Editar Cliente", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = redColor)
            )
        }
    ) { padding ->
        if (currentClient == null && uiState is ClientsUiState.Loading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = redColor)
                Text("Buscando datos...", modifier = Modifier.padding(top = 16.dp))
            }
        } else if (currentClient == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No se pudo cargar la información del cliente")
            }
        } else {
            val client = currentClient!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // seccion de la foto
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 24.dp)
                        .clickable { launcher.launch("image/*") }
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        // Si se está subiendo una foto podríamos mostrar un overlay de carga aquí
                        if (client.imagenUrl.isNotEmpty()) {
                            AsyncImage(
                                model = client.imagenUrl,
                                contentDescription = "Imagen del establecimiento",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = null,
                                        tint = Color.DarkGray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text("Toca para añadir foto", color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }

                // FORMULARIO DE EDICIÓN
                OutlinedTextField(
                    value = editedNombre,
                    onValueChange = { editedNombre = it },
                    label = { Text("Nombre del Cliente") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Ej: Calle 123, Quilmes, Buenos Aires",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = editedDireccion,
                        onValueChange = { editedDireccion = it },
                        label = { Text("Dirección Exacta") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = editedReparto,
                    onValueChange = { editedReparto = it },
                    label = { Text("Número de Reparto") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val repartoInt = editedReparto.toIntOrNull() ?: currentClient?.reparto ?: 0
                        viewModel.updateClientBasicData(clientId, editedNombre, editedDireccion, repartoInt)
                        onSaveSuccess()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = redColor)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Cambios", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                if (uiState is ClientsUiState.Error) {
                    Text(
                        text = uiState.message,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}
