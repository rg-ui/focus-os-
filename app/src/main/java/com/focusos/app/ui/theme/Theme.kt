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
    secondary = StatusTeal,
    tertiary = StatusPurple,
    background = AppleDarkBg,
    surface = AppleDarkSurface,
    surfaceVariant = AppleDarkElevated,
    onPrimary = AppleDarkTextPrimary,
    onSecondary = AppleDarkTextPrimary,
    onBackground = AppleDarkTextPrimary,
    onSurface = AppleDarkTextPrimary,
    onSurfaceVariant = AppleDarkTextSecondary,
    outline = AppleDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlueLight,
    secondary = StatusTeal,
    tertiary = StatusPurple,
    background = AppleLightBg,
    surface = AppleLightSurface,
    surfaceVariant = AppleLightElevated,
    onPrimary = AppleLightTextPrimary,
    onSecondary = AppleLightTextPrimary,
    onBackground = AppleLightTextPrimary,
    onSurface = AppleLightTextPrimary,
    onSurfaceVariant = AppleLightTextSecondary,
    outline = AppleLightBorder
)

@Composable
fun FocusOsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
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
