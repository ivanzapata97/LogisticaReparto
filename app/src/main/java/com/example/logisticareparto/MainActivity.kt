package com.example.logisticareparto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.logisticareparto.features.navigation.AppNavigation
import com.example.logisticareparto.notifications.RouteNotificationHelper
import com.example.logisticareparto.ui.theme.LogisticaRepartoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RouteNotificationHelper.ensureChannel(this)
        setContent {
            LogisticaRepartoTheme {
                AppNavigation()
            }
        }
    }
}
