package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TactileRoyalBlue
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextWhite

enum class NavigationTab(val label: String, val icon: ImageVector, val tag: String) {
    BATTERY("Battery", Icons.Default.Bolt, "nav_battery"),
    STREAKS("Streaks", Icons.Default.Shield, "nav_streaks"),
    PRIZE_ROAD("Prize Road", Icons.Default.MilitaryTech, "nav_prize_road"),
    TECH_CREW("Tech Crew", Icons.Default.Engineering, "nav_tech_crew"),
    MY_SPACE("My Space", Icons.Default.QueryStats, "nav_my_space")
}

/**
 * Soft Frosted Light Glass & Neumorphic Floating Navigation Bar
 */
@Composable
fun FloatingGlassNavBar(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false,
    accentColor: Color = TactileRoyalBlue
) {
    val barBackground = if (isDarkMode) Color(0xF01E293B) else Color(0xEBFFFFFF)
    val shadowColor = if (isDarkMode) Color(0x66000000) else Color(0x2B9AAEC4)
    val borderColor = if (isDarkMode) Color(0x33475569) else Color(0x66E2E8F0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .graphicsLayer { clip = false },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // Dual Soft Neumorphic Drop Shadow
                .drawBehind {
                    drawRoundRect(
                        color = shadowColor,
                        topLeft = Offset(0f, 6.dp.toPx()),
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(26.dp.toPx(), 26.dp.toPx())
                    )
                }
                .clip(RoundedCornerShape(26.dp))
                .background(barBackground)
                .border(
                    width = 1.2.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab

                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) accentColor else TextSlate,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "tabIconColor"
                    )

                    val pillBackground by animateColorAsState(
                        targetValue = if (isSelected) {
                            if (isDarkMode) accentColor.copy(alpha = 0.22f) else Color(0xFFF1F5F9)
                        } else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "tabPillBg"
                    )

                    val pillBorderColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            if (isDarkMode) accentColor.copy(alpha = 0.5f) else Color(0x66CBD5E1)
                        } else Color.Transparent,
                        label = "tabPillBorder"
                    )

                    Box(
                        modifier = Modifier
                            .testTag(tab.tag)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(18.dp))
                            .background(pillBackground)
                            .border(
                                width = 1.dp,
                                color = pillBorderColor,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onTabSelected(tab)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.label,
                                fontFamily = OutfitFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp,
                                letterSpacing = 0.2.sp,
                                color = if (isSelected) {
                                    if (isDarkMode) TextWhite else TextCharcoal
                                } else TextSlate,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
