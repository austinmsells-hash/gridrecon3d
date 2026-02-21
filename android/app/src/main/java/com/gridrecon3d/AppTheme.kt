package com.gridrecon3d

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TacticalDarkColors = darkColorScheme(
    primary = Color(0xFFFF2D2D),
    secondary = Color(0xFFFF2D2D),
    tertiary = Color(0xFF8A8A8A),
    background = Color(0xFF000000),
    surface = Color(0xFF0B0F14),
    onPrimary = Color(0xFF000000),
    onSecondary = Color(0xFF000000),
    onBackground = Color(0xFFEAEAEA),
    onSurface = Color(0xFFEAEAEA)
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TacticalDarkColors,
        typography = Typography(),
        content = content
    )
}
