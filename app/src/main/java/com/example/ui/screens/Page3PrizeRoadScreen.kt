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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GlassThemeConfig
import com.example.data.PrizeNode
import com.example.data.PrizeRoadData
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.PulsingNeonDot
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPhosphorusGreen
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TextLuminous
import com.example.ui.theme.TextMutedGreen
import java.text.NumberFormat
import java.util.Locale

/**
 * PAGE 3: Prize Road
 * 1,000 Levels based on cumulative mAh up to 10,000,000 mAh.
 * Milestones every 5 levels.
 */
@Composable
fun Page3PrizeRoadScreen(
    prizeRoadData: PrizeRoadData,
    themeConfig: GlassThemeConfig,
    onClaimMilestone: (Int) -> Unit,
    onAddSimulatedMah: (Long) -> Unit
) {
    val accentColor = themeConfig.accentColor
    val glow = themeConfig.glowIntensity
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    // Generate milestone nodes every 5 levels up to Level 1,000
    // Center view around current level
    val currentLvl = prizeRoadData.currentLevel
    val startLevel = ((currentLvl - 25) / 5 * 5).coerceAtLeast(5)
    val endLevel = (startLevel + 60).coerceAtMost(1000)

    val milestoneList = remember(startLevel, endLevel) {
        val list = mutableListOf<PrizeNode>()
        for (lvl in startLevel..endLevel step 5) {
            val reqMah = lvl * 10_000L
            val rewardName = when {
                lvl % 100 == 0 -> "Quantum Matrix Core Theme + 3 Golden Shields"
                lvl % 50 == 0 -> "Cryo Superconductor Skin + Master Title"
                lvl % 25 == 0 -> "Cyber Overdrive Reactor Module"
                lvl % 10 == 0 -> "+1 Permanent Shield Capacity"
                else -> "5,000 Egg XP Catalyst + Neon Badge"
            }
            list.add(
                PrizeNode(
                    level = lvl,
                    requiredCumulativeMah = reqMah,
                    title = "Milestone Tier #$lvl",
                    rewardName = rewardName,
                    rewardType = if (lvl % 50 == 0) "THEME" else "SHIELD",
                    isMilestone = true,
                    iconKey = "tier_$lvl"
                )
            )
        }
        list
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 110.dp)
            .graphicsLayer {
                // 90 FPS rendering isolation
                clip = false
            }
    ) {
        // Hero Level & Cumulative mAh Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = accentColor,
                    glowIntensity = glow
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
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
                                        .background(accentColor.copy(alpha = 0.2f))
                                        .border(1.dp, accentColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MilitaryTech,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "1,000-LEVEL PRIZE ROAD",
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        letterSpacing = 1.sp,
                                        color = TextLuminous
                                    )
                                    Text(
                                        text = "Rank: ${prizeRoadData.activeTitle}",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 11.sp,
                                        color = accentColor
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "LVL ${prizeRoadData.currentLevel}",
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = TextLuminous
                                )
                                Text(
                                    text = "OF 1,000 LEVELS",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 9.sp,
                                    color = TextMutedGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Cumulative mAh Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "CUMULATIVE CHARGE PROCESSED",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextMutedGreen
                            )
                            Text(
                                text = "${numberFormat.format(prizeRoadData.cumulativeMah)} / 10,000,000 mAh",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = accentColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val progressRatio = (prizeRoadData.cumulativeMah.toFloat() / 10_000_000f).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = accentColor,
                            trackColor = Color(0x3310241C)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Test Simulator Button to add mAh
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1 Level = 10,000 mAh | Milestones every 5 lvls",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 10.sp,
                                color = TextMutedGreen
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(accentColor.copy(alpha = 0.2f))
                                    .border(1.dp, accentColor, RoundedCornerShape(10.dp))
                                    .clickable { onAddSimulatedMah(50_000L) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+50k mAh BOOST",
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = accentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Section Title: Milestones Track
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "5-LEVEL MILESTONE REWARDS",
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                    color = TextLuminous
                )
                PulsingNeonDot(color = accentColor, sizeDp = 6.dp)
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Milestone Nodes List
        items(milestoneList, key = { it.level }) { node ->
            val isUnlocked = prizeRoadData.currentLevel >= node.level
            val isClaimed = prizeRoadData.claimedMilestones.contains(node.level)
            val isCurrentTarget = !isUnlocked && node.level == ((prizeRoadData.currentLevel / 5 + 1) * 5)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp)
            ) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = if (isUnlocked && !isClaimed) Color(0xFF00FF87) else if (isCurrentTarget) NeonCyan else Color(0x66446655),
                    glowIntensity = if (isUnlocked && !isClaimed) 1.0f else 0.7f
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Level Badge Circle
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isClaimed -> Color(0x3300FF87)
                                        isUnlocked -> Color(0x5500FF87)
                                        else -> Color(0x221A221E)
                                    }
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = when {
                                        isClaimed -> Color(0xFF00FF87)
                                        isUnlocked -> Color(0xFF00FF87)
                                        else -> Color(0x4486A396)
                                    },
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isClaimed) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Claimed",
                                    tint = Color(0xFF00FF87),
                                    modifier = Modifier.size(20.dp)
                                )
                            } else if (!isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = TextMutedGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "Ready",
                                    tint = Color(0xFF00FF87),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Node Title & Reward
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "LEVEL ${node.level} MILESTONE",
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isUnlocked) TextLuminous else TextMutedGreen
                                )
                                Text(
                                    text = "${numberFormat.format(node.requiredCumulativeMah)} mAh",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 10.sp,
                                    color = if (isUnlocked) accentColor else TextMutedGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = node.rewardName,
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.sp,
                                color = if (isUnlocked && !isClaimed) Color(0xFF00FF87) else TextMutedGreen,
                                lineHeight = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Action / Status Pill
                        when {
                            isClaimed -> {
                                Text(
                                    text = "CLAIMED",
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF00FF87)
                                )
                            }
                            isUnlocked -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF00FF87))
                                        .clickable { onClaimMilestone(node.level) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "CLAIM",
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                            else -> {
                                Text(
                                    text = "LOCKED",
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = TextMutedGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
