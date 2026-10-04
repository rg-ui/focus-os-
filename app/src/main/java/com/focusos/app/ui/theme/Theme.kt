package com.focusos.app.ui.theme

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
    primary = AccentBlue,
    secondary = AccentCyan,
    tertiary = AccentPurple,
    background = GlassBgDark,
    surface = GlassDarkSurface,
    surfaceVariant = GlassDarkCard,
    onPrimary = GlassDarkTextPrimary,
    onSecondary = GlassDarkTextPrimary,
    onBackground = GlassDarkTextPrimary,
    onSurface = GlassDarkTextPrimary,
    onSurfaceVariant = GlassDarkTextSecondary,
    outline = GlassDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlueLight,
    secondary = StatusTeal,
    tertiary = AccentPurple,
    background = GlassLightBg,
    surface = GlassLightSurface,
    surfaceVariant = GlassLightCard,
    onPrimary = GlassLightTextPrimary,
    onSecondary = GlassLightTextPrimary,
    onBackground = GlassLightTextPrimary,
    onSurface = GlassLightTextPrimary,
    onSurfaceVariant = GlassLightTextSecondary,
    outline = GlassLightBorder
)

@Composable
fun FocusOsTheme(
    darkTheme: Boolean = true, // Default to stunning Glassmorphic Dark UI
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = GlassBgDark.toArgb()
            window.navigationBarColor = GlassBgDark.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
