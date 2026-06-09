package com.example.logisticareparto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cloudinary.android.MediaManager
import com.example.logisticareparto.features.navigation.AppNavigation
import com.example.logisticareparto.notifications.RouteNotificationHelper
import com.example.logisticareparto.ui.theme.LogisticaRepartoTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RouteNotificationHelper.ensureChannel(this)
        MediaManager.init(this)
        setContent {
            LogisticaRepartoTheme {
                AppNavigation()
            }
        }
    }
}
