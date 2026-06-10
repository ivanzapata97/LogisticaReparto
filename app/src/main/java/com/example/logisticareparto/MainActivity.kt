package com.example.logisticareparto

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cloudinary.android.MediaManager
import com.example.logisticareparto.features.navigation.AppNavigation
import com.example.logisticareparto.notifications.RouteNotificationHelper
import com.example.logisticareparto.ui.theme.LogisticaRepartoTheme
import com.example.logisticareparto.utils.LocaleHelper
import java.util.Locale

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

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val langCode = prefs.getString("app_language", "es") ?: "es"
        val locale = LocaleHelper.localeFromCode(langCode)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocales(LocaleList(locale))
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }
}
