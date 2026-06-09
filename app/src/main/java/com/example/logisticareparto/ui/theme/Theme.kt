package com.example.logisticareparto.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = CokeRed,
    onPrimary = Color.White,
    primaryContainer = CokeRedLight,
    onPrimaryContainer = CokeRedDark,
    secondary = DarkGray,
    onSecondary = Color.White,
    secondaryContainer = LightGray,
    onSecondaryContainer = Charcoal,
    tertiary = MediumGray,
    onTertiary = Color.White,
    background = OffWhite,
    onBackground = Charcoal,
    surface = Color.White,
    onSurface = Charcoal,
    surfaceVariant = LightGray,
    onSurfaceVariant = DarkGray,
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFFD1D1D6)
)

private val DarkColorScheme = darkColorScheme(
    primary = CokeRed,
    onPrimary = Color.White,
    primaryContainer = CokeRedDark,
    onPrimaryContainer = CokeRedLight,
    secondary = DarkGray,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3A3A3C),
    onSecondaryContainer = LightGray,
    tertiary = MediumGray,
    onTertiary = Color.Black,
    background = Color(0xFF000000),
    onBackground = Color(0xFFF2F2F7),
    surface = Color(0xFF1C1C1E),
    onSurface = Color(0xFFF2F2F7),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFC7C7CC),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFF48484A)
)

@Composable
fun LogisticaRepartoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
