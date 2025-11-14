package com.tesfk.extereplugin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7C74FF),
    onPrimary = Color.Black,
    secondary = Color(0xFF3DC5FF),
    background = Color(0xFF05030A),
    surface = Color(0xFF0B0A13),
    onSurface = Color(0xFFE7E5FF)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF4335F0),
    onPrimary = Color.White,
    secondary = Color(0xFF0084FF),
    background = Color(0xFFF0EFFA),
    surface = Color.White,
    onSurface = Color(0xFF1D1A2E)
)

@Composable
fun ExterePluginTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
