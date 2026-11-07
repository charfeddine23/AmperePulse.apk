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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FireExtinguisher
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.TechCrewMember
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.PulsingNeonDot
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TextLuminous
import com.example.ui.theme.TextMutedGreen

/**
 * PAGE 4: Tech Crew
 * Interactive Technicians, Cyber Heroes, and Firefighter cooling units.
 */
@Composable
fun Page4TechCrewScreen(
    crewMembers: List<TechCrewMember>,
    themeConfig: GlassThemeConfig,
    onTriggerAbility: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val accentColor = themeConfig.accentColor
    val glow = themeConfig.glowIntensity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 110.dp)
            .graphicsLayer {
                // 90 FPS rendering isolation
                clip = false
            }
    ) {
        // Hero Crew Station Banner
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
                                    imageVector = Icons.Default.Engineering,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "CYBER TECH CREW DECK",
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = 1.sp,
                                    color = TextLuminous
                                )
                                Text(
                                    text = "Specialist Operatives & Coolant Units",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.sp,
                                    color = TextMutedGreen
                                )
                            }
                        }

                        PulsingNeonDot(color = accentColor, sizeDp = 7.dp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Deploy specialized tactical abilities to suppress battery thermals, eliminate phantom parasitic drain, inject harmonic XP, and reinforce battery health shields.",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = TextMutedGreen,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Crew Members Cards List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            crewMembers.forEach { member ->
                TechCrewCard(
                    member = member,
                    glow = glow,
                    onDeploy = { onTriggerAbility(member.id) }
                )
            }
        }
    }
}

@Composable
fun TechCrewCard(
    member: TechCrewMember,
    glow: Float,
    onDeploy: () -> Unit
) {
    val isCoolingDown = member.cooldownRemainingSec > 0
    val isActive = member.isActiveAbilityRunning

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = member.primaryColor,
        glowIntensity = if (isActive) 1.0f else glow
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar Box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(member.primaryColor.copy(alpha = 0.22f))
                        .border(1.5.dp, member.primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.avatarLetter,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member.name,
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextLuminous
                    )
                    Text(
                        text = member.title,
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 10.sp,
                        color = member.primaryColor
                    )
                }

                // Active / Ready Badge
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(member.primaryColor.copy(alpha = 0.25f))
                            .border(1.dp, member.primaryColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ACTIVE (${member.activeSecondsRemaining}s)",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = member.primaryColor
                        )
                    }
                } else if (isCoolingDown) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33446655))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "COOLDOWN ${member.cooldownRemainingSec}s",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = TextMutedGreen
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(member.primaryColor.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "READY",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = member.primaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ability Title & Description
            Text(
                text = "SPECIAL ABILITY: ${member.abilityName.uppercase()}",
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.8.sp,
                color = TextLuminous
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = member.abilityDescription,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 11.sp,
                color = TextMutedGreen,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cooldown progress bar if on cooldown
            if (isCoolingDown) {
                val progress = 1f - (member.cooldownRemainingSec.toFloat() / member.cooldownTotalSec)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = member.primaryColor,
                    trackColor = Color(0x3310241C)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Action Deploy Button
            Button(
                onClick = onDeploy,
                enabled = !isCoolingDown && !isActive,
                colors = ButtonDefaults.buttonColors(
                    containerColor = member.primaryColor,
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0x221B2822),
                    disabledContentColor = Color(0x5586A396)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("btn_deploy_${member.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isActive) "UNIT CURRENTLY ACTIVE" else if (isCoolingDown) "RECHARGING THERMAL MATRIX..." else "DEPLOY ${member.abilityName.uppercase()}",
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
