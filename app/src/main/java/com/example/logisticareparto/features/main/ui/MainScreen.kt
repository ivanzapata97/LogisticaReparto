package com.example.logisticareparto.features.main.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.logisticareparto.features.auth.viewmodel.AuthViewModel
import com.example.logisticareparto.features.clients.ui.ClientsScreen
import com.example.logisticareparto.features.clients.viewmodel.ClientsViewModel
import com.example.logisticareparto.features.clients.ui.create.ClientCreateScreen
import com.example.logisticareparto.features.clients.ui.detail.ClientDetailScreen
import com.example.logisticareparto.features.clients.ui.edit.ClientEditScreen
import com.example.logisticareparto.features.profile.ui.ProfileScreen
import com.example.logisticareparto.features.route.ui.RouteScreen
import com.example.logisticareparto.features.search.ui.SearchScreen

@Composable
fun MainScreen(
    authViewModel: AuthViewModel, 
    clientsViewModel: ClientsViewModel, 
    onLogout: () -> Unit,
    onChangeTruck: () -> Unit
) {
    val navController = rememberNavController()
    val terracottaRed = MaterialTheme.colorScheme.secondary

    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Inicio", "Buscar", "Ruta", "Perfil")
    val icons = listOf(Icons.Default.Assignment, Icons.Default.Search, Icons.Default.Route, Icons.Default.Person)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color.Gray
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            when (index) {
                                0 -> navController.navigate("inicio") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                                1 -> navController.navigate("buscar") {
                                    launchSingleTop = true
                                }
                                2 -> navController.navigate("ruta") {
                                    launchSingleTop = true
                                }
                                3 -> navController.navigate("perfil") {
                                    launchSingleTop = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = terracottaRed,
                            selectedTextColor = terracottaRed,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = terracottaRed.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "inicio",
            modifier = Modifier.padding(padding)
        ) {
            composable("inicio") {
                ClientsScreen(
                    viewModel = clientsViewModel,
                    onClientClick = { clientId -> 
                        navController.navigate("detalle_cliente/$clientId")
                    },
                    onBack = null
                )
            }
            composable("buscar") {
                SearchScreen(
                    viewModel = clientsViewModel,
                    onClientClick = { clientId -> 
                        navController.navigate("detalle_cliente/$clientId")
                    },
                    onAddClientClick = { navController.navigate("crear_cliente") },
                    onOpenRouteClick = {
                        selectedItem = 2
                        navController.navigate("ruta") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable("ruta") {
                RouteScreen(
                    viewModel = clientsViewModel,
                    onClientClick = { clientId -> 
                        navController.navigate("detalle_cliente/$clientId")
                    }
                )
            }
            composable("perfil") {
                ProfileScreen(
                    authViewModel = authViewModel, 
                    clientsViewModel = clientsViewModel,
                    onChangeTruck = onChangeTruck,
                    onLogout = onLogout
                )
            }
            composable("detalle_cliente/{clientId}") { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("clientId") ?: ""
                ClientDetailScreen(
                    clientId = clientId,
                    viewModel = clientsViewModel,
                    onBack = { navController.popBackStack() },
                    onEditClick = { navController.navigate("edit_cliente/$clientId") }
                )
            }
            composable("edit_cliente/{clientId}") { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("clientId") ?: ""
                ClientEditScreen(
                    clientId = clientId,
                    viewModel = clientsViewModel,
                    onBack = { navController.popBackStack() },
                    onSaveSuccess = { 
                        navController.popBackStack("inicio", inclusive = false)
                    }
                )
            }
            composable("crear_cliente") {
                ClientCreateScreen(
                    viewModel = clientsViewModel,
                    onBack = { navController.popBackStack() },
                    onSuccess = {
                        navController.popBackStack("buscar", inclusive = false)
                    }
                )
            }
        }
    }
}
