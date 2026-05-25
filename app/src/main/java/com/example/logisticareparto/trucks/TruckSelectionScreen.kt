package com.example.logisticareparto.trucks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TruckSelectionScreen(onTruckSelected: (Int) -> Unit) {
    val redColor = Color(0xFFE30613)
    val trucks = listOf(5, 12, 16, 21, 31, 76, 80, 85, 86, 88, 99, 145, 146)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Selecciona tu Camión", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = redColor)
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
                imageVector = Icons.Default.LocalShipping,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = redColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "¿En qué camión estás hoy?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(trucks) { truck ->
                    Button(
                        onClick = { onTruckSelected(truck) },
                        modifier = Modifier
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = redColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text(
                            text = truck.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
