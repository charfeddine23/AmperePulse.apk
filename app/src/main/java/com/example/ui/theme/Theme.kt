package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AmpereDarkColorScheme = darkColorScheme(
    primary = NeonPhosphorusGreen,
    onPrimary = ObsidianOled,
    primaryContainer = Color(0x3300FF87),
    onPrimaryContainer = NeonPhosphorusGreen,
    secondary = NeonCyan,
    onSecondary = ObsidianOled,
    secondaryContainer = Color(0x3300E5FF),
    onSecondaryContainer = NeonCyan,
    tertiary = ElectricMint,
    onTertiary = ObsidianOled,
    background = ObsidianOled,
    onBackground = TextLuminous,
    surface = ObsidianDarkSurface,
    onSurface = TextLuminous,
    surfaceVariant = ObsidianCardGlass,
    onSurfaceVariant = TextMutedGreen,
    outline = ObsidianCardGlassBorder,
    outlineVariant = Color(0x2200FF87)
)

@Composable
fun MyApplicationTheme(
    accentColor: Color = NeonPhosphorusGreen,
    content: @Composable () -> Unit
) {
    val dynamicColorScheme = AmpereDarkColorScheme.copy(
        primary = accentColor,
        onPrimaryContainer = accentColor,
        primaryContainer = accentColor.copy(alpha = 0.2f),
        outline = accentColor.copy(alpha = 0.35f)
    )

    MaterialTheme(
        colorScheme = dynamicColorScheme,
        typography = Typography,
        content = content
    )
}
