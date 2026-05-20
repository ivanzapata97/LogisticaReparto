package com.example.logisticareparto.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import com.example.logisticareparto.auth.AuthViewModel
import com.example.logisticareparto.auth.LoginScreen

@Composable
fun AppNavigation(){
    val navController = rememberNavController()
    val authViewModel : AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login"){
        //ruta 1 Login
        composable("login"){
            LoginScreen(viewModel = authViewModel) {
                navController.navigate("seleccion_camion"){
                    popUpTo("login") { inclusive = true}
                }
            }
        }

        composable("seleccion_camion") {
            PantallaSeleccionTemporal()
        }
    }
}

@Composable
fun PantallaSeleccionTemporal(){
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Text(text = "Login Exitoso, aca va la seleccion de camion")
    }
}