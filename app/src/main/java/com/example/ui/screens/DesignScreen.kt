package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GlassThemeConfig
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidTopAppBar
import com.example.ui.components.PulsingNeonDot
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPhosphorusGreen
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TextLuminous
import com.example.ui.theme.TextMutedGreen

@Composable
fun DesignScreen(
    themeConfig: GlassThemeConfig,
    onConfigChange: (GlassThemeConfig) -> Unit
) {
    val scrollState = rememberScrollState()
    val accentColor = themeConfig.accentColor
    val glow = themeConfig.glowIntensity

    val accentHues = listOf(
        Pair("Phosphorus Green", NeonPhosphorusGreen),
        Pair("Neon Cyan", NeonCyan),
        Pair("Cyber Violet", CyberViolet),
        Pair("Solar Amber", SolarAmber)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 110.dp)
    ) {
        LiquidTopAppBar(
            title = "M5 DESIGN STUDIO",
            subtitle = "LIQUID GLASS REFRACTION & GLOW LAB",
            accentColor = accentColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Interactive Live Glass Preview Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = accentColor,
                glowIntensity = glow
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LIVE GLASS SPECIMEN",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextLuminous
                            )
                        }
                        PulsingNeonDot(color = accentColor, sizeDp = 8.dp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Liquid Glass M5 aesthetic features deep dark translucent obsidian glass (#0B0E0D), frosted inner shadows, and vivid blurry glowing neon edges (#00FF87) with real-time refraction physics.",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        color = TextMutedGreen,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MiniSpecChip(label = "ACCENT", value = themeConfig.accentName)
                        MiniSpecChip(label = "GLOW LEVEL", value = "${(glow * 100).toInt()}%")
                        MiniSpecChip(label = "REFRACTION", value = if (themeConfig.ambientLiquidWaves) "ACTIVE" else "STATIC")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Accent Hue Customizer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = accentColor,
                glowIntensity = glow
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "NEON PHOSPHORUS ACCENT HUE",
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        color = TextLuminous
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select radiant phosphorus chromatic emission color",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = TextMutedGreen
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        accentHues.forEachIndexed { index, pair ->
                            val isSelected = themeConfig.accentColorIndex == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) pair.second.copy(alpha = 0.22f) else Color(0x66121C18))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) pair.second else Color(0x33446655),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        onConfigChange(themeConfig.copy(accentColorIndex = index))
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(pair.second),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = pair.first.split(" ").last(),
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) TextLuminous else TextMutedGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Glow Intensity & Refraction Sliders
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = accentColor,
                glowIntensity = glow
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "NEON GLOW & LUMINESCENCE",
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        color = TextLuminous
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Edge Glow Intensity",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 12.sp,
                            color = TextMutedGreen
                        )
                        Text(
                            text = "${(glow * 100).toInt()}%",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = accentColor
                        )
                    }

                    Slider(
                        value = glow,
                        onValueChange = { onConfigChange(themeConfig.copy(glowIntensity = it)) },
                        valueRange = 0.3f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = Color(0x33446655)
                        ),
                        modifier = Modifier.testTag("slider_glow_intensity")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Liquid waves toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Liquid Wave Dynamics",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = TextLuminous
                            )
                            Text(
                                text = "Simulate fluid sine oscillation inside circular ring",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.sp,
                                color = TextMutedGreen
                            )
                        }
                        Switch(
                            checked = themeConfig.ambientLiquidWaves,
                            onCheckedChange = { onConfigChange(themeConfig.copy(ambientLiquidWaves = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = accentColor,
                                uncheckedThumbColor = TextMutedGreen,
                                uncheckedTrackColor = Color(0x33446655)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preset Themes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = accentColor,
                glowIntensity = glow
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "CURATED M5 PRESETS",
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        color = TextLuminous
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetButton(
                            title = "PHOSPHORUS",
                            sub = "Default M5",
                            color = NeonPhosphorusGreen,
                            onClick = {
                                onConfigChange(
                                    themeConfig.copy(
                                        accentColorIndex = 0,
                                        glowIntensity = 0.85f,
                                        ambientLiquidWaves = true
                                    )
                                )
                            }
                        )
                        PresetButton(
                            title = "CYBER CYAN",
                            sub = "Electric Port",
                            color = NeonCyan,
                            onClick = {
                                onConfigChange(
                                    themeConfig.copy(
                                        accentColorIndex = 1,
                                        glowIntensity = 0.95f,
                                        ambientLiquidWaves = true
                                    )
                                )
                            }
                        )
                        PresetButton(
                            title = "VIOLET RAY",
                            sub = "Quantum High",
                            color = CyberViolet,
                            onClick = {
                                onConfigChange(
                                    themeConfig.copy(
                                        accentColorIndex = 2,
                                        glowIntensity = 0.90f,
                                        ambientLiquidWaves = true
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniSpecChip(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 9.sp,
            color = TextMutedGreen
        )
        Text(
            text = value,
            fontFamily = RajdhaniFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = TextLuminous
        )
    }
}

@Composable
private fun PresetButton(
    title: String,
    sub: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x55111B17))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = color
            )
            Text(
                text = sub,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 9.sp,
                color = TextMutedGreen
            )
        }
    }
}
