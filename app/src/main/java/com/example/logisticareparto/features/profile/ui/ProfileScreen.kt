package com.example.logisticareparto.features.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logisticareparto.features.auth.viewmodel.AuthViewModel
import androidx.compose.material.icons.filled.LocalShipping
import com.example.logisticareparto.features.clients.viewmodel.ClientsViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.example.logisticareparto.R
import com.example.logisticareparto.utils.LocaleHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel, 
    clientsViewModel: ClientsViewModel, 
    onChangeTruck: () -> Unit,
    onLogout: () -> Unit
) {
    val user = remember { Firebase.auth.currentUser }
    val primaryColor = MaterialTheme.colorScheme.primary
    
    val currentLang = clientsViewModel.currentLanguage
    val stats = clientsViewModel.driverStats
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        clientsViewModel.loadDriverStats()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.title_profile), fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = primaryColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = primaryColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = user?.email ?: stringResource(R.string.label_user_placeholder),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.label_assigned_truck, clientsViewModel.selectedTruck),
                style = MaterialTheme.typography.titleMedium,
                color = primaryColor
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            if (stats != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Estadísticas",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatItem(value = "${stats.totalRoutes}", label = "Rutas totales")
                            StatItem(value = "${stats.totalFinishedRoutes}", label = "Rutas finalizadas")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatItem(value = "${stats.totalStops}", label = "Paradas totales")
                            StatItem(value = "${stats.totalClientsVisited}", label = "Clientes visitados")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // idioma
            var showLangDialog by remember { mutableStateOf(false) }

            OutlinedButton(
                onClick = { showLangDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor)
            ) {
                Text(stringResource(R.string.label_app_language))
            }

            if (showLangDialog) {
                AlertDialog(
                    onDismissRequest = { showLangDialog = false },
                    title = { Text(stringResource(R.string.label_app_language)) },
                    text = {
                        Column {
                            TextButton(
                                onClick = {
                                    clientsViewModel.setLanguage("es")
                                    LocaleHelper.updateLocale(context, "es")
                                    showLangDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    stringResource(R.string.lang_es),
                                    color = if (currentLang == "es") MaterialTheme.colorScheme.primary else Color.Unspecified,
                                    fontWeight = if (currentLang == "es") FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            TextButton(
                                onClick = {
                                    clientsViewModel.setLanguage("en")
                                    LocaleHelper.updateLocale(context, "en")
                                    showLangDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    stringResource(R.string.lang_en),
                                    color = if (currentLang == "en") MaterialTheme.colorScheme.primary else Color.Unspecified,
                                    fontWeight = if (currentLang == "en") FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    },
                    confirmButton = {}
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // cambio de camion
            OutlinedButton(
                onClick = onChangeTruck,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor)
            ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_change_truck))
            }

            Spacer(modifier = Modifier.height(16.dp))

            //cerrar sesion
            Button(
                onClick = { 
                    authViewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_logout))
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

