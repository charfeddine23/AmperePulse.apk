package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightNeumorphicColorScheme = lightColorScheme(
    primary = TactileRoyalBlue,
    onPrimary = OnAccentLight,
    primaryContainer = Color(0x222962FF),
    onPrimaryContainer = TactileRoyalBlue,
    secondary = TactileGreen,
    onSecondary = OnAccentLight,
    secondaryContainer = Color(0x2200C853),
    onSecondaryContainer = TactileGreen,
    tertiary = TactileCoralRed,
    onTertiary = OnAccentLight,
    background = NeumorphBgLight,
    onBackground = TextCharcoal,
    surface = NeumorphSurfaceLight,
    onSurface = TextCharcoal,
    surfaceVariant = NeumorphInsetLight,
    onSurfaceVariant = TextSlate,
    outline = NeumorphBorderLight,
    outlineVariant = Color(0x1A2962FF)
)

private val DarkNeumorphicColorScheme = darkColorScheme(
    primary = Color(0xFF448AFF),
    onPrimary = Color.Black,
    primaryContainer = Color(0x33448AFF),
    onPrimaryContainer = Color(0xFF82B1FF),
    secondary = Color(0xFF00E676),
    onSecondary = Color.Black,
    secondaryContainer = Color(0x3300E676),
    onSecondaryContainer = Color(0xFF69F0AE),
    tertiary = Color(0xFFFF5722),
    onTertiary = Color.Black,
    background = NeumorphBgDark,
    onBackground = TextWhite,
    surface = NeumorphSurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = Color(0xFF27354A),
    onSurfaceVariant = TextMutedDark,
    outline = NeumorphBorderDark,
    outlineVariant = Color(0x33448AFF)
)

@Composable
fun MyApplicationTheme(
    isDarkMode: Boolean = false,
    accentColor: Color = TactileRoyalBlue,
    content: @Composable () -> Unit
) {
    val baseScheme = if (isDarkMode) DarkNeumorphicColorScheme else LightNeumorphicColorScheme
    val colorScheme = baseScheme.copy(
        primary = accentColor,
        onPrimaryContainer = accentColor,
        primaryContainer = accentColor.copy(alpha = 0.15f)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
