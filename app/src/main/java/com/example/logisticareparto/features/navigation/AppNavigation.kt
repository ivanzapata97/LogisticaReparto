package com.example.logisticareparto.features.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.logisticareparto.data.repository.AuthRepository
import com.example.logisticareparto.data.repository.ClientRepository
import com.example.logisticareparto.data.repository.RouteRepository
import com.example.logisticareparto.features.auth.ui.LoginScreen
import com.example.logisticareparto.features.auth.ui.RegisterScreen
import com.example.logisticareparto.features.auth.viewmodel.AuthViewModel
import com.example.logisticareparto.features.clients.viewmodel.ClientsViewModel
import com.example.logisticareparto.features.main.ui.MainScreen
import com.example.logisticareparto.features.trucks.ui.TruckSelectionScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val authRepository = remember { AuthRepository() }
    val clientRepository = remember { ClientRepository() }
    val routeRepository = remember { RouteRepository() }

    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(authRepository) as T
            }
        }
    )

    val clientsViewModel: ClientsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ClientsViewModel(clientRepository, routeRepository) as T
            }
        }
    )

    val startDestination = if (authRepository.getCurrentUser() != null) "truck_selection" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") }
            ) {
                navController.navigate("truck_selection") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }

        composable("register") {
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
