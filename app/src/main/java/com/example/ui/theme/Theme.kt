package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ActivaNeonCyan,
    onPrimary = CockpitBackground,
    primaryContainer = CockpitCard,
    onPrimaryContainer = ActivaNeonCyan,
    secondary = ActivaOrange,
    onSecondary = CockpitBackground,
    secondaryContainer = CockpitCard,
    onSecondaryContainer = ActivaOrange,
    tertiary = ActivaEcoGreen,
    background = CockpitBackground,
    surface = CockpitSurface,
    surfaceVariant = CockpitCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = CockpitCardBorder
)

private val LightColorScheme = darkColorScheme(
    primary = ActivaNeonCyan,
    onPrimary = CockpitBackground,
    secondary = ActivaOrange,
    background = CockpitBackground,
    surface = CockpitSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek automotive cockpit theme
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = CockpitBackground.toArgb()
                it.navigationBarColor = CockpitBackground.toArgb()
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
