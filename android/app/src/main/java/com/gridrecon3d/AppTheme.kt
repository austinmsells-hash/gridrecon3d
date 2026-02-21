package com.gridrecon3d

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TacticalDarkColors = darkColorScheme(
    primary = Color(0xFFFF2D2D),
    secondary = Color(0xFFFF2D2D),
    tertiary = Color(0xFFB0B0B0),
    background = Color(0xFF000000),
    surface = Color(0xFF0B0F14),
    surfaceVariant = Color(0xFF121822),
    onPrimary = Color(0xFF000000),
    onSecondary = Color(0xFF000000),
    onBackground = Color(0xFFEAEAEA),
    onSurface = Color(0xFFEAEAEA),
    onSurfaceVariant = Color(0xFFD6D6D6),
    outline = Color(0xFF2A3442),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TacticalDarkColors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}
