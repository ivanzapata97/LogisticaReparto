package com.example.logisticareparto.clients.create

import android.net.Uri
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
import com.example.logisticareparto.clients.ClientsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientCreateScreen(viewModel: ClientsViewModel, onBack: () -> Unit, onSuccess: () -> Unit) {
    val redColor = Color(0xFFE30613)
    val context = LocalContext.current
    
    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var contacto by remember { mutableStateOf("") }
    var cuil by remember { mutableStateOf("") }
    var dias by remember { mutableStateOf("") }
    var reparto by remember { mutableStateOf("") }
    var apertura by remember { mutableStateOf("") }
    var cierre by remember { mutableStateOf("") }
    var es24 by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nuevo Cliente", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = redColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // SECCIÓN FOTO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(bottom = 24.dp)
                    .clickable { launcher.launch("image/*") }
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(40.dp))
                                Text("Añadir foto (Opcional)", color = Color.DarkGray, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // FORMULARIO
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre / Razón Social") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ej: Calle 123, Quilmes, Buenos Aires",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
                OutlinedTextField(
                    value = direccion, 
                    onValueChange = { direccion = it }, 
                    label = { Text("Dirección Exacta") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ej: 11223344, 11556677 (separados por coma)",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
                OutlinedTextField(
                    value = contacto, 
                    onValueChange = { contacto = it }, 
                    label = { Text("Teléfono de Contacto") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ej: 20334445551, 27334445552",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
                OutlinedTextField(
                    value = cuil, 
                    onValueChange = { cuil = it }, 
                    label = { Text("CUIL") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ej: Lunes, Miércoles, Viernes",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
                OutlinedTextField(
                    value = dias, 
                    onValueChange = { dias = it }, 
                    label = { Text("Días de visita") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = reparto,
                onValueChange = { reparto = it },
                label = { Text("Número de Reparto") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = es24, onCheckedChange = { es24 = it }, colors = CheckboxDefaults.colors(checkedColor = redColor))
                Text("Atención 24hs")
            }

            if (!es24) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = apertura, onValueChange = { apertura = it }, label = { Text("Abre") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(value = cierre, onValueChange = { cierre = it }, label = { Text("Cierra") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val isFormValid = nombre.isNotEmpty() && 
                             direccion.isNotEmpty() && 
                             dias.isNotEmpty() && 
                             (es24 || (apertura.isNotEmpty() && cierre.isNotEmpty()))

            Button(
                onClick = {
                    val nuevoCliente = Client(
                        cliente = nombre,
                        direccion = direccion,
                        contacto = contacto.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        cuil = cuil.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        dias = dias.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        reparto = reparto.toIntOrNull() ?: 0,
                        apertura = apertura,
                        cierre = cierre,
                        es24 = es24
                    )
                    viewModel.createClient(context, nuevoCliente, imageUri)
                    onSuccess()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = redColor),
                enabled = isFormValid
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear Cliente", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
