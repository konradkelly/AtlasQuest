package com.atlasquest.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AtlasBlue = Color(0xFF1565C0)
private val AtlasGold = Color(0xFFFFC107)
private val AtlasTeal = Color(0xFF00796B)

private val LightColorScheme = lightColorScheme(
    primary = AtlasBlue,
    secondary = AtlasTeal,
    tertiary = AtlasGold,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    secondary = Color(0xFF80CBC4),
    tertiary = AtlasGold,
)

@Composable
fun AtlasQuestTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
