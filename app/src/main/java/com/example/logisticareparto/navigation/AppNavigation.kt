package com.example.logisticareparto.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.logisticareparto.auth.AuthViewModel
import com.example.logisticareparto.auth.LoginScreen
import com.example.logisticareparto.auth.RegisterScreen
import com.example.logisticareparto.clients.ClientsViewModel
import com.example.logisticareparto.main.MainScreen
import com.example.logisticareparto.trucks.TruckSelectionScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(){
    val navController = rememberNavController()
    val authViewModel : AuthViewModel = viewModel()
    val clientsViewModel: ClientsViewModel = viewModel()
    
    // Verificar si el usuario ya está logueado para persistencia de sesion
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) "truck_selection" else "login"

    NavHost(navController = navController, startDestination = startDestination){
        //Login
        composable("login"){
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") }
            ) {
                navController.navigate("truck_selection"){
                    popUpTo("login") { inclusive = true}
                }
            }
        }

        //Registro
        composable("register"){
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate("truck_selection") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        //Elección de Camión
        composable("truck_selection") {
            TruckSelectionScreen(
                onTruckSelected = { truck ->
                    clientsViewModel.setTruck(truck)
                    navController.navigate("main") {
                        popUpTo("truck_selection") { inclusive = true }
                    }
                }
            )
        }

        // Ruta Principal 
        composable("main") {
            MainScreen(
                authViewModel = authViewModel,
                clientsViewModel = clientsViewModel,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onChangeTruck = {
                    navController.navigate("truck_selection") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}