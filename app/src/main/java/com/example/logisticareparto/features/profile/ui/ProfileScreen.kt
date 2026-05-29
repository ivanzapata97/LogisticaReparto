package com.example.logisticareparto.features.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logisticareparto.features.auth.viewmodel.AuthViewModel
import androidx.compose.material.icons.filled.LocalShipping
import com.example.logisticareparto.features.clients.viewmodel.ClientsViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel, 
    clientsViewModel: ClientsViewModel, 
    onChangeTruck: () -> Unit,
    onLogout: () -> Unit
) {
    val user = remember { Firebase.auth.currentUser }
    val redColor = Color(0xFFE30613)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = redColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = redColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = user?.email ?: "Usuario",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Camión Asignado: ${clientsViewModel.selectedTruck}",
                fontSize = 16.sp,
                color = redColor,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // boton Cambiar Camión
            OutlinedButton(
                onClick = onChangeTruck,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = redColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, redColor)
            ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cambiar Camión")
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { 
                    authViewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = redColor),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }
        }
    }
}
