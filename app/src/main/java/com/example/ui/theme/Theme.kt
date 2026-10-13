package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = TextPrimary,
    primaryContainer = SurfaceCardHigh,
    onPrimaryContainer = TextPrimary,
    secondary = NeonMagenta,
    onSecondary = TextPrimary,
    secondaryContainer = SurfaceCard,
    onSecondaryContainer = TextSecondary,
    tertiary = ElectricCyan,
    onTertiary = TextInverse,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = BackgroundSurface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for consistent futuristic neon aesthetic
    dynamicColor: Boolean = false, // Keep signature MYRA AI neon branding
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = BackgroundDark.toArgb()
                window.navigationBarColor = BackgroundDark.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
