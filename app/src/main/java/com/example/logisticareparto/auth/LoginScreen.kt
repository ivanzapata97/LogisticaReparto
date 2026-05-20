package com.example.logisticareparto.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(viewModel: AuthViewModel,onLoginSucess: () -> Unit){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var uiState = viewModel.uiState

    //controlamos el popup para error de inicio de sesion
    var showDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Error){
            //traducimoos el error de fb a un popup
            errorMessage = (if (uiState.message.contains("badly formatted") || uiState.message.contains("invalid")){
                "El usuario o contraseña son incorrectos."
            }else{
                "No se pudo iniciar sesión. Revisa la conexión"
            })
            showDialog = true
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Logistica de Reparto", style = MaterialTheme.typography.headlineLarge)
        Text(text = "Ingreso", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(32.dp))

        //email
        OutlinedTextField(
            value = email,
            onValueChange = {email = it},
            label = {Text("Correo Electronico")},
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Gray,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        //contraseña
        OutlinedTextField(
            value = password,
            onValueChange = {password = it},
            label = {Text("Contraseña")},
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Gray,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {viewModel.login(email, password)},
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is AuthUiState.Loading
        ) {
            if (uiState is AuthUiState.Loading){
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            }else {
                Text("Entrar")
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