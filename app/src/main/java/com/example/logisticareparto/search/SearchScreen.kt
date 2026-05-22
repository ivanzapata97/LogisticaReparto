package com.example.logisticareparto.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.logisticareparto.clients.ClientItem
import com.example.logisticareparto.clients.ClientsUiState
import com.example.logisticareparto.clients.ClientsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: ClientsViewModel, onClientClick: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState = viewModel.uiState
    val redColor = Color(0xFFE30613)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Buscar Clientes", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = redColor)
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
                placeholder = { Text("Nombre o dirección...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = redColor,
                    cursorColor = redColor
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (uiState is ClientsUiState.Success) {
                val filteredClients = uiState.clients.filter {
                    it.cliente.contains(searchQuery, ignoreCase = true) || 
                    it.direccion.contains(searchQuery, ignoreCase = true)
                }
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredClients) { client ->
                        ClientItem(client, onClick = { onClientClick(client.id) })
                    }
                }
            }
        }
    }
}
