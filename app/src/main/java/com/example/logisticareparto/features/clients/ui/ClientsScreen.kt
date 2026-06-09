package com.example.logisticareparto.features.clients.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logisticareparto.R
import com.example.logisticareparto.data.models.Client
import com.example.logisticareparto.data.models.ScheduleState
import com.example.logisticareparto.features.clients.viewmodel.ClientsUiState
import com.example.logisticareparto.features.clients.viewmodel.ClientsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(viewModel: ClientsViewModel, onClientClick: (String) -> Unit, onBack: (() -> Unit)? = null) {
    val uiState = viewModel.uiState
    val coralRed = MaterialTheme.colorScheme.primary
    val terracottaRed = MaterialTheme.colorScheme.secondary

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.clients_title), 
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = terracottaRed
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is ClientsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = coralRed
                    )
                }
                is ClientsUiState.Error -> {
                    Text(
                        text = uiState.message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is ClientsUiState.Success -> {
                    val filteredClients = viewModel.getFilteredClients(uiState.clients)
                    
                    if (filteredClients.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_clients_truck_today),
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Gray
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
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
    }
}

@Composable
fun ClientItem(
    client: Client,
    onClick: () -> Unit,
    containerColor: Color = Color.White,
    actionContent: (@Composable RowScope.() -> Unit)? = null
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = client.cliente,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = client.direccion,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                val scheduleState = client.getScheduleState()
                val (horarioTexto, estaAbierto) = when (scheduleState) {
                    is ScheduleState.Open24h -> stringResource(R.string.status_open_24h) to true
                    is ScheduleState.OpenNow -> stringResource(R.string.status_open_now, scheduleState.closeTime) to true
                    is ScheduleState.ClosedNow -> stringResource(R.string.status_closed_now, scheduleState.openTime) to false
                    is ScheduleState.NotSpecified -> stringResource(R.string.label_not_specified) to false
                    is ScheduleState.Range -> stringResource(R.string.status_schedule_range, scheduleState.openTime, scheduleState.closeTime) to false
                }

                Text(
                    text = horarioTexto,
                    fontSize = 12.sp,
                    color = if (estaAbierto) Color(0xFF4CAF50) else Color(0xFFFF5252),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (actionContent != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    content = actionContent
                )
            }
        }
    }
}
