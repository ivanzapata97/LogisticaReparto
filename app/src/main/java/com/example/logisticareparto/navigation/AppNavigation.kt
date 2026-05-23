package com.example.logisticareparto.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.logisticareparto.auth.AuthViewModel
import com.example.logisticareparto.auth.LoginScreen
import com.example.logisticareparto.auth.RegisterScreen
import com.example.logisticareparto.main.MainScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(){
    val navController = rememberNavController()
    val authViewModel : AuthViewModel = viewModel()
    
    // Verificar si el usuario ya está logueado para persistencia de sesion
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) "main" else "login"

    NavHost(navController = navController, startDestination = startDestination){
        //Login
        composable("login"){
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") }
            ) {
                navController.navigate("main"){
                    popUpTo("login") { inclusive = true}
                }
            }
        }

        //Registro
        composable("register"){
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate("main") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // Ruta Principal 
        composable("main") {
            MainScreen(
                authViewModel = authViewModel,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}