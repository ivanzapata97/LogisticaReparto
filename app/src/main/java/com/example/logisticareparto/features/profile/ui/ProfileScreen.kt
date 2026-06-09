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
import com.example.logisticareparto.R
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel, 
    clientsViewModel: ClientsViewModel, 
    onChangeTruck: () -> Unit,
    onLogout: () -> Unit
) {
    val user = remember { Firebase.auth.currentUser }
    val coralRed = MaterialTheme.colorScheme.primary
    val terracottaRed = MaterialTheme.colorScheme.secondary
    
    val currentLang = clientsViewModel.currentLanguage

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.title_profile), fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = terracottaRed)
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
                tint = terracottaRed
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = user?.email ?: stringResource(R.string.label_user_placeholder),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.label_assigned_truck, clientsViewModel.selectedTruck),
                fontSize = 16.sp,
                color = terracottaRed,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Selector de Idioma
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.label_app_language),
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LanguageOption(
                            label = stringResource(R.string.lang_es),
                            isSelected = currentLang == "es",
                            modifier = Modifier.weight(1f)
                        ) {
                            clientsViewModel.setLanguage("es")
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"))
                        }
                        LanguageOption(
                            label = stringResource(R.string.lang_en),
                            isSelected = currentLang == "en",
                            modifier = Modifier.weight(1f)
                        ) {
                            clientsViewModel.setLanguage("en")
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // boton Cambiar Camión
            OutlinedButton(
                onClick = onChangeTruck,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = terracottaRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, terracottaRed)
            ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_change_truck))
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { 
                    authViewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = coralRed),
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
fun LanguageOption(
    label: String, 
    isSelected: Boolean, 
    modifier: Modifier = Modifier, 
    onClick: () -> Unit
) {
    val color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
    
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, color),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = bgColor, contentColor = color)
    ) {
        Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
