package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = IceBluePrimary,
    onPrimary = Obsidian950,
    primaryContainer = IceBlueContainer,
    onPrimaryContainer = IceBlueLight,
    secondary = GlacierTeal,
    onSecondary = Obsidian950,
    secondaryContainer = Obsidian700,
    onSecondaryContainer = FrostWhite,
    tertiary = IceBlueLight,
    onTertiary = Obsidian950,
    background = Obsidian950,
    onBackground = FrostWhite,
    surface = Obsidian900,
    onSurface = FrostWhite,
    surfaceVariant = Obsidian800,
    onSurfaceVariant = MutedSlate,
    outline = Obsidian600,
    outlineVariant = Obsidian700
)

@Composable
fun KalistenTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Obsidian950.toArgb()
                window.navigationBarColor = Obsidian950.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
