package com.example.logisticareparto.features.search.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.logisticareparto.features.clients.ui.ClientItem
import com.example.logisticareparto.features.clients.viewmodel.ClientsUiState
import com.example.logisticareparto.features.clients.viewmodel.ClientsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: ClientsViewModel,
    onClientClick: (String) -> Unit,
    onAddClientClick: () -> Unit,
    onOpenRouteClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState = viewModel.uiState
    val coralRed = MaterialTheme.colorScheme.primary
    val terracottaRed = MaterialTheme.colorScheme.secondary
    val routeCount = viewModel.routeDraft.size
    val hasActiveRoute = viewModel.hasActiveRoute

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Clientes", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    IconButton(onClick = onAddClientClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar Cliente",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = terracottaRed)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nombre o direccion...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = coralRed,
                    cursorColor = coralRed
                )
            )

            if (hasActiveRoute) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE9F7EE))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Ya hay una ruta iniciada",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                text = "Marca visitas o finalizala antes de armar otra",
                                color = Color.DarkGray
                            )
                        }
                        Button(
                            onClick = onOpenRouteClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Icon(Icons.Default.Route, contentDescription = null)
                            Text("Ir a ruta")
                        }
                    }
                }
            } else if (routeCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDEBEC))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Hoja de ruta en progreso",
                                fontWeight = FontWeight.Bold,
                                color = terracottaRed
                            )
                            Text(
                                text = "$routeCount parada(s) listas para ordenar",
                                color = Color.DarkGray
                            )
                        }
                        Button(
                            onClick = onOpenRouteClick,
                            colors = ButtonDefaults.buttonColors(containerColor = coralRed)
                        ) {
                            Icon(Icons.Default.Route, contentDescription = null)
                            Text("Ver ruta")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState is ClientsUiState.Success) {
                val allClients = viewModel.getFilteredClients(
                    uiState.clients,
                    onlyTruck = false,
                    onlyToday = false
                )

                val filteredClients = allClients.filter {
                    it.cliente.contains(searchQuery, ignoreCase = true) ||
                        it.direccion.contains(searchQuery, ignoreCase = true) ||
                        it.codigoCliente.contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredClients) { client ->
                        val isInRoute = viewModel.isClientInRoute(client.id)
                        ClientItem(
                            client = client,
                            onClick = { onClientClick(client.id) },
                            actionContent = {
                                if (hasActiveRoute) {
                                    Button(
                                        onClick = onOpenRouteClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = terracottaRed)
                                    ) {
                                        Text("Ruta en curso")
                                    }
                                } else if (isInRoute) {
                                    Button(
                                        onClick = onOpenRouteClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = terracottaRed)
                                    ) {
                                        Text("Ya esta en ruta")
                                    }
                                } else if (routeCount > 0) {
                                    OutlinedButton(
                                        onClick = { viewModel.addClientToRoute(client) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = coralRed),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, coralRed)
                                    ) {
                                        Text("Sumar a ruta")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
