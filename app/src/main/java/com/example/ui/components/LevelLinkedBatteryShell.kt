package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PrizeRoadData
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.TactileAmber
import com.example.ui.theme.TactileGreen
import com.example.ui.theme.TactileRoyalBlue
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextWhite
import java.text.NumberFormat
import java.util.Locale

/**
 * Level-Linked Luxury Battery Component
 * Directly bound to the user's progress in the 1,000-Level Prize Road (up to 10,000,000 mAh).
 * Features a tactile extruded neumorphic shell, terminal cathode cap, and 10 glowing internal energy cell segments.
 */
@Composable
fun LevelLinkedBatteryShell(
    prizeRoadData: PrizeRoadData,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false,
    glowIntensity: Float = 0.8f,
    onAddSimulatedMah: (Long) -> Unit
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    val currentLevel = prizeRoadData.currentLevel
    val cumulativeMah = prizeRoadData.cumulativeMah
    val maxMah = 10_000_000L

    // Progress percentage across 1,000 levels
    val overallProgress = (cumulativeMah.toFloat() / maxMah.toFloat()).coerceIn(0f, 1f)

    // Number of active illuminated cells (out of 10)
    val activeCellCount = (overallProgress * 10).toInt().coerceIn(1, 10)

    // Next milestone every 5 levels
    val nextMilestoneLevel = ((currentLevel / 5) + 1) * 5
    val nextMilestoneMah = nextMilestoneLevel * 10_000L
    val mahToNextMilestone = (nextMilestoneMah - cumulativeMah).coerceAtLeast(0L)

    val isEliteTier = currentLevel >= 100
    val tierGold = TactileAmber

    val infiniteTransition = rememberInfiniteTransition(label = "batteryGlow")
    val cellPulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cellPulse"
    )

    val cardBg = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val shadowColor = if (isDarkMode) Color(0x66000000) else Color(0x2B9AAEC4)
    val highlightColor = if (isDarkMode) Color(0x1F94A3B8) else Color(0xE6FFFFFF)
    val textColor = if (isDarkMode) TextWhite else TextCharcoal

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("level_linked_battery_shell")
            // Dual Neumorphic Shadows
            .drawBehind {
                val shadowOffset = 5.dp.toPx()
                val highlightOffset = -3.dp.toPx()

                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(shadowOffset, shadowOffset),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx())
                )
                drawRoundRect(
                    color = highlightColor,
                    topLeft = Offset(highlightOffset, highlightOffset),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .border(
                width = 1.2.dp,
                color = if (isEliteTier) tierGold.copy(alpha = 0.45f) else if (isDarkMode) Color(0x33475569) else Color(0x4DE2E8F0),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
    ) {
        Column {
            // Header Row: Level Badge & Grandeur Rank
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isEliteTier) tierGold.copy(alpha = if (isDarkMode) 0.25f else 0.15f)
                                else accentColor.copy(alpha = if (isDarkMode) 0.22f else 0.12f)
                            )
                            .border(
                                1.dp,
                                if (isEliteTier) tierGold else accentColor,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEliteTier) Icons.Default.WorkspacePremium else Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = if (isEliteTier) tierGold else accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LEVEL-LINKED LUXURY CELL",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp,
                                color = textColor
                            )
                            if (isEliteTier) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(tierGold.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ELITE TIER",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = tierGold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Rank: ${prizeRoadData.activeTitle}",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            color = if (isEliteTier) tierGold else accentColor
                        )
                    }
                }

                // Level Readout
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "LVL $currentLevel",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = if (isEliteTier) tierGold else textColor
                    )
                    Text(
                        text = "OF 1,000 LEVELS",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp,
                        color = TextSlate
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PHYSICAL TACTILE BATTERY CASING WITH 10 ILLUMINATED CELLS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Terminal Cathode Anode Cap (Physical metallic protrusion)
                Box(
                    modifier = Modifier
                        .size(10.dp, 24.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFCBD5E1),
                                    Color(0xFF94A3B8),
                                    Color(0xFF64748B)
                                )
                            )
                        )
                )

                // Battery Body Chamber Frame
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                        .border(
                            width = 1.5.dp,
                            color = if (isEliteTier) tierGold.copy(alpha = 0.6f) else if (isDarkMode) Color(0x44475569) else Color(0x66CBD5E1),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    // 10 Segmented Glowing Energy Cells
                    Row(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 1..10) {
                            val isCellActive = i <= activeCellCount
                            val isCurrentHead = i == activeCellCount

                            val cellColor = if (isEliteTier) tierGold else accentColor
                            val cellBrush = if (isCellActive) {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.9f),
                                        cellColor,
                                        cellColor.copy(alpha = if (isCurrentHead) 0.7f * cellPulse else 0.85f)
                                    )
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        if (isDarkMode) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                                        if (isDarkMode) Color(0xFF16202E) else Color(0xFFD6DFEB)
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(cellBrush)
                                    .then(
                                        if (isCurrentHead && isCellActive) {
                                            Modifier.border(
                                                1.dp,
                                                Color.White.copy(alpha = cellPulse),
                                                RoundedCornerShape(6.dp)
                                            )
                                        } else Modifier
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Metrics & Cumulative mAh Readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CUMULATIVE ENERGY PROCESSED",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = TextSlate
                    )
                    Text(
                        text = "${numberFormat.format(cumulativeMah)} / 10,000,000 mAh",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isEliteTier) tierGold else accentColor
                    )
                }

                // Next milestone indicator
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "NEXT MILESTONE: LVL $nextMilestoneLevel",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = TextSlate
                    )
                    Text(
                        text = "${numberFormat.format(mahToNextMilestone)} mAh to reach",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        color = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Simulator Advancement Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bound to Prize Road: 1 Level = 10,000 mAh",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 10.sp,
                    color = TextSlate
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            (if (isEliteTier) tierGold else accentColor).copy(
                                alpha = if (isDarkMode) 0.22f else 0.12f
                            )
                        )
                        .border(
                            1.dp,
                            if (isEliteTier) tierGold else accentColor,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onAddSimulatedMah(50_000L) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (isEliteTier) tierGold else accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+50k mAh BOOST",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (isEliteTier) tierGold else accentColor
                        )
                    }
                }
            }
        }
    }
}
