package com.example.logisticareparto.features.auth.ui

import com.example.logisticareparto.features.auth.viewmodel.AuthUiState
import com.example.logisticareparto.features.auth.viewmodel.AuthViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(viewModel: AuthViewModel, onNavigateToRegister: () -> Unit, onLoginSucess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val uiState = viewModel.uiState
    val redColor = Color(0xFFE30613)

    // controlamos el popup para error de inicio de sesion
    var showDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Error) {
            // traducimos el error de fb a un popup
            errorMessage = (if (uiState.message.contains("badly formatted") || uiState.message.contains("invalid")) {
                "El usuario o contraseña son incorrectos."
            } else {
                "No se pudo iniciar sesión. Revisa la conexión"
            })
            showDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono Rojo de Camión
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(20.dp),
                color = redColor,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Gestión de reparto",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
            )

            Text(
                text = "Ingresar",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Campo Usuario/Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Usuario o email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = redColor,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = redColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = redColor,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = redColor
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Ingresar
            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState !is AuthUiState.Loading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = redColor,
                    contentColor = Color.White
                )
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        "Ingresar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Texto Registrate
            TextButton(onClick = { onNavigateToRegister() }) {
                Text(
                    text = "Crear cuenta",
                    color = redColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    //resultado
    if (showDialog){
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                viewModel.resetState()
            },
            title = {Text(text = "Error de Ingreso")},
            text = {
                Text(text = errorMessage)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        viewModel.resetState()
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
    when (uiState) {
        is AuthUiState.Success -> {
            LaunchedEffect(Unit) {
                onLoginSucess()
            }
        }
        else -> {}
    }
}