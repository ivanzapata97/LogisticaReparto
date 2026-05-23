package com.example.logisticareparto.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.logisticareparto.auth.AuthViewModel
import com.example.logisticareparto.clients.ClientsScreen
import com.example.logisticareparto.clients.ClientsViewModel
import com.example.logisticareparto.clients.detail.ClientDetailScreen
import com.example.logisticareparto.clients.edit.ClientEditScreen
import com.example.logisticareparto.profile.ProfileScreen
import com.example.logisticareparto.search.SearchScreen

@Composable
fun MainScreen(authViewModel: AuthViewModel, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val clientsViewModel: ClientsViewModel = viewModel()
    val redColor = Color(0xFFE30613)

    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Inicio", "Buscar", "Perfil")
    val icons = listOf(Icons.Default.Assignment, Icons.Default.Search, Icons.Default.Person)

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
                                2 -> navController.navigate("perfil") {
                                    launchSingleTop = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = redColor,
                            selectedTextColor = redColor,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFFFDEBEC)
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
                    }
                )
            }
            composable("perfil") {
                ProfileScreen(authViewModel = authViewModel, onLogout = onLogout)
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
        }
    }
}
