package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.ThemePalette

// Immersive UI Dark Theme (Default Enterprise Aesthetic)
private val SlateScheme = darkColorScheme(
    primary = Indigo500,
    secondary = Indigo400,
    tertiary = Violet500,
    background = Neutral950,
    surface = Slate900,
    surfaceVariant = Slate800,
    onPrimary = Color.White,
    onBackground = Slate100,
    onSurface = Slate100,
    onSurfaceVariant = Slate400,
    outline = BorderWhite10
)

private val EmeraldScheme = lightColorScheme(
    primary = Color(0xFF059669),
    secondary = Color(0xFF0284C7),
    tertiary = Color(0xFFD97706),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE2E8F0),
    onPrimary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

private val CyberScheme = darkColorScheme(
    primary = Color(0xFF00F0FF),
    secondary = Color(0xFFFF0055),
    tertiary = Color(0xFFCCFF00),
    background = Color(0xFF0A0A12),
    surface = Color(0xFF141424),
    surfaceVariant = Color(0xFF232338),
    onPrimary = Color.Black,
    onBackground = Color(0xFF00F0FF),
    onSurface = Color(0xFFE0F8FF)
)

private val SunsetScheme = darkColorScheme(
    primary = Color(0xFFFF6B6B),
    secondary = Color(0xFFFFD166),
    tertiary = Color(0xFF06D6A0),
    background = Color(0xFF1A1016),
    surface = Color(0xFF2D1B28),
    surfaceVariant = Color(0xFF4A2B42),
    onPrimary = Color.Black,
    onBackground = Color(0xFFFFF0F5),
    onSurface = Color(0xFFFFF0F5)
)

@Composable
fun AIMeetTheme(
    palette: ThemePalette = ThemePalette.SLATE_DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (palette) {
        ThemePalette.SLATE_DARK -> SlateScheme
        ThemePalette.EMERALD_LIGHT -> EmeraldScheme
        ThemePalette.CYBER_NEON -> CyberScheme
        ThemePalette.SUNSET_GLOW -> SunsetScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
