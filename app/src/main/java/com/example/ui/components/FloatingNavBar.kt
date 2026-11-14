package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CyberHapticsEngine
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TactileRoyalBlue
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlin.math.abs
import kotlin.math.sqrt

enum class NavigationTab(val label: String, val arabicLabel: String, val icon: ImageVector, val tag: String) {
    BATTERY("Battery", "البطارية", Icons.Default.Bolt, "nav_battery"),
    BATTERY_SOH("Cell SOH", "صحة الخلية", Icons.Default.HealthAndSafety, "nav_soh"),
    CABLE_DNA("Cable DNA", "كابل DNA", Icons.Default.Cable, "nav_cable_dna"),
    MY_SPACE("My Space", "مساحتي", Icons.Default.Tune, "nav_my_space");

    fun getLocalizedLabel(isArabic: Boolean): String = if (isArabic) arabicLabel else label
}

/**
 * Real-time coupled two-mass spring-damper physics engine for the Liquid Glass Droplet.
 * All animated fields are Compose FloatState read exclusively inside Canvas draw scope,
 * ensuring 120 FPS zero-recomposition GPU rendering.
 */
@Stable
private class LiquidDropletPhysicsState {
    var headX by mutableFloatStateOf(0f)
    var tailX by mutableFloatStateOf(0f)
    var fluidLift by mutableFloatStateOf(0f)
    var wobble by mutableFloatStateOf(0f)

    var targetHeadX by mutableFloatStateOf(0f)
    var isInitialized by mutableStateOf(false)

    // Internal simulation velocities (non-state to avoid unnecessary snapshots)
    private var headVel = 0f
    private var tailVel = 0f
    private var liftVel = 0f
    private var wobbleVel = 0f
    private var prevHeadVel = 0f

    fun snapAllTo(x: Float) {
        headX = x
        tailX = x
        targetHeadX = x
        fluidLift = 0f
        wobble = 0f
        headVel = 0f
        tailVel = 0f
        liftVel = 0f
        wobbleVel = 0f
        prevHeadVel = 0f
        isInitialized = true
    }

    fun triggerSurfaceRipple(impulse: Float) {
        wobbleVel += impulse.coerceIn(-4.2f, 4.2f)
    }

    /**
     * Advances the coupled fluid physics by [dtSeconds] using 2 RK-like symplectic sub-steps.
     * Returns true if simulation is still actively moving, false once completely at rest.
     */
    fun step(dtSeconds: Float, isDragging: Boolean): Boolean {
        val dt = dtSeconds.coerceIn(0.001f, 0.025f)
        val subDt = dt * 0.5f

        val headStiffness = if (isDragging) 1450f else 330f
        val headDamping = if (isDragging) 2f * 0.90f * sqrt(headStiffness) else 2f * 0.72f * sqrt(headStiffness)

        // Softer viscous spring between headX and tailX creates authentic liquid inertia & tail lag
        val tailStiffness = if (isDragging) 235f else 210f
        val tailDamping = if (isDragging) 2f * 0.72f * sqrt(tailStiffness) else 2f * 0.66f * sqrt(tailStiffness)

        val speedFactor = (abs(headVel) / 1400f).coerceIn(0f, 1f)
        val targetLift = if (isDragging) 1f else (speedFactor * 0.9f).coerceIn(0f, 1f)
        val liftStiffness = 280f
        val liftDamping = 2f * 0.80f * sqrt(liftStiffness)

        // Surface tension harmonic oscillation (natural liquid slosh on deceleration)
        val wobbleStiffness = 240f
        val wobbleDamping = 2f * 0.44f * sqrt(wobbleStiffness)

        repeat(2) {
            // 1. Head mass tracks finger or target tab center
            val headAcc = (targetHeadX - headX) * headStiffness - headVel * headDamping
            headVel += headAcc * subDt
            headX += headVel * subDt

            // Feed rapid deceleration into subtle liquid squash-and-stretch ripple
            val deltaVel = headVel - prevHeadVel
            if (!isDragging && abs(deltaVel) > 65f) {
                wobbleVel += (-deltaVel / 18000f).coerceIn(-0.45f, 0.45f)
            }
            prevHeadVel = headVel

            // 2. Trailing fluid mass follows headX with viscous lag (naturally stretches & flips)
            val tailAcc = (headX - tailX) * tailStiffness - tailVel * tailDamping
            tailVel += tailAcc * subDt
            tailX += tailVel * subDt

            // 3. Capillary elevation / lift state
            val liftAcc = (targetLift - fluidLift) * liftStiffness - liftVel * liftDamping
            liftVel += liftAcc * subDt
            fluidLift = (fluidLift + liftVel * subDt).coerceIn(0f, 1.15f)

            // 4. Surface tension squash/stretch wobble
            val wobbleAcc = -wobble * wobbleStiffness - wobbleVel * wobbleDamping
            wobbleVel += wobbleAcc * subDt
            wobble = (wobble + wobbleVel * subDt).coerceIn(-0.22f, 0.22f)
        }

        val isSettled = !isDragging &&
            abs(targetHeadX - headX) < 0.20f &&
            abs(headVel) < 1.5f &&
            abs(headX - tailX) < 0.20f &&
            abs(tailVel) < 1.5f &&
            abs(fluidLift) < 0.004f &&
            abs(liftVel) < 0.01f &&
            abs(wobble) < 0.002f &&
            abs(wobbleVel) < 0.01f

        if (isSettled) {
            headX = targetHeadX
            tailX = targetHeadX
            headVel = 0f
            tailVel = 0f
            fluidLift = 0f
            liftVel = 0f
            wobble = 0f
            wobbleVel = 0f
            return false
        }
        return true
    }
}

/**
 * Apple Liquid Glass Floating Navigation Bar with Realistic Fluid Droplet Physics.
 *
 * Uses a continuous two-mass spring-damper simulation (`headX` + viscous `tailX`)
 * plus volume conservation and surface-tension slosh, synced to native display vsync
 * at 120 FPS with zero Compose recomposition during motion.
 */
@Composable
fun FloatingGlassNavBar(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false,
    accentColor: Color = TactileRoyalBlue,
    showLiquidDroplet: Boolean = true
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val barBackground = if (isDarkMode) Color(0xEC1A2334) else Color(0xECE3E9F2)
    val shadowColor = if (isDarkMode) Color(0x55000000) else Color(0x3B7589A2)
    val borderColor = if (isDarkMode) Color(0x38475569) else Color(0x99B8C5D6)

    val maxStretchPx = with(density) { 46.dp.toPx() }
    val physics = remember { LiquidDropletPhysicsState() }

    var isDragging by remember { mutableStateOf(false) }
    var activeHoveredIndex by remember { mutableIntStateOf(selectedTab.ordinal) }
    var physicsEpoch by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .height(62.dp)
            .graphicsLayer { clip = false },
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRoundRect(
                        color = shadowColor,
                        topLeft = Offset(0f, 6.dp.toPx()),
                        size = size,
                        cornerRadius = CornerRadius(26.dp.toPx(), 26.dp.toPx())
                    )
                }
                .clip(RoundedCornerShape(26.dp))
                .background(barBackground)
                .border(
                    width = 1.15.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            val barWidthPx = constraints.maxWidth.toFloat()
            val totalTabs = NavigationTab.entries.size
            val tabWidthPx = if (totalTabs > 0) barWidthPx / totalTabs else 1f

            fun tabCenterX(index: Int): Float {
                val visualIndex = if (isRtl) (totalTabs - 1 - index) else index
                return (visualIndex + 0.5f) * tabWidthPx
            }

            fun xToTabIndex(x: Float): Int {
                val visualSlot = (x / tabWidthPx).toInt().coerceIn(0, totalTabs - 1)
                return if (isRtl) (totalTabs - 1 - visualSlot) else visualSlot
            }

            // Initialize / resize placement cleanly
            LaunchedEffect(barWidthPx, isRtl) {
                if (barWidthPx > 0f) {
                    val center = tabCenterX(selectedTab.ordinal)
                    if (!physics.isInitialized) {
                        physics.snapAllTo(center)
                        activeHoveredIndex = selectedTab.ordinal
                    } else if (!isDragging) {
                        physics.targetHeadX = center
                        physicsEpoch++
                    }
                }
            }

            // Sync external tab taps into physical glide
            LaunchedEffect(selectedTab) {
                if (physics.isInitialized && !isDragging) {
                    activeHoveredIndex = selectedTab.ordinal
                    val targetX = tabCenterX(selectedTab.ordinal)
                    if (abs(targetX - physics.targetHeadX) > 1f) {
                        physics.targetHeadX = targetX
                        physics.triggerSurfaceRipple(0.85f)
                        physicsEpoch++
                    }
                }
            }

            // Frame-synced 120 FPS fluid simulation loop (runs ONLY while actively animating or dragging)
            LaunchedEffect(physicsEpoch, isDragging, barWidthPx) {
                if (!physics.isInitialized || barWidthPx <= 0f) return@LaunchedEffect
                var lastNanos = 0L
                while (isActive) {
                    var keepRunning = true
                    withFrameNanos { frameNanos ->
                        val dt = if (lastNanos == 0L) 0.016f else (frameNanos - lastNanos) / 1_000_000_000f
                        lastNanos = frameNanos
                        keepRunning = physics.step(dt, isDragging)
                    }
                    if (!keepRunning) break
                }
            }

            // Interactive Continuous Pointer Gesture Handler
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(barWidthPx, isRtl) {
                        coroutineScope {
                            while (isActive) {
                                awaitPointerEventScope {
                                    val down = awaitFirstDown(
                                        requireUnconsumed = false,
                                        pass = PointerEventPass.Initial
                                    )
                                    isDragging = true
                                    physicsEpoch++

                                    val startX = down.position.x
                                    val initialClampedX = startX.coerceIn(tabWidthPx * 0.32f, barWidthPx - tabWidthPx * 0.32f)
                                    physics.targetHeadX = initialClampedX
                                    physics.triggerSurfaceRipple(-1.15f)

                                    val initialIndex = xToTabIndex(initialClampedX)
                                    activeHoveredIndex = initialIndex

                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                        if (!change.pressed) break

                                        val currentX = change.position.x
                                        val clampedX = currentX.coerceIn(tabWidthPx * 0.32f, barWidthPx - tabWidthPx * 0.32f)
                                        physics.targetHeadX = clampedX

                                        val hoveredIndex = xToTabIndex(clampedX)
                                        if (hoveredIndex != activeHoveredIndex) {
                                            activeHoveredIndex = hoveredIndex
                                            CyberHapticsEngine.triggerTactileTick(context)
                                            physics.triggerSurfaceRipple(0.95f)
                                            onTabSelected(NavigationTab.entries[hoveredIndex])
                                        }

                                        change.consume()
                                    }

                                    // Finger released: spring smoothly to final tab center with gentle liquid slosh
                                    isDragging = false
                                    val finalIndex = activeHoveredIndex
                                    val finalTab = NavigationTab.entries[finalIndex]
                                    physics.targetHeadX = tabCenterX(finalIndex)
                                    physics.triggerSurfaceRipple(1.45f)
                                    physicsEpoch++

                                    CyberHapticsEngine.triggerTactileTick(context)
                                    onTabSelected(finalTab)
                                }
                            }
                        }
                    }
            ) {
                // Layer 1: Realistic Fluid Glass Droplet Canvas (draw-only invalidation)
                if (showLiquidDroplet) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRealisticLiquidGlassDroplet(
                            headX = physics.headX,
                            tailX = physics.tailX,
                            fluidLift = physics.fluidLift,
                            wobble = physics.wobble,
                            centerY = size.height / 2f,
                            maxStretchPx = maxStretchPx,
                            isDarkMode = isDarkMode,
                            accentColor = accentColor,
                            baseHeight = 44.dp.toPx().coerceAtMost(size.height - 4.dp.toPx())
                        )
                    }
                }

                // Layer 2: Interactive Tabs Row (Icons and Labels)
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavigationTab.entries.forEachIndexed { index, tab ->
                        val isCurrentActive = if (isDragging) index == activeHoveredIndex else tab == selectedTab

                        val iconColor by animateColorAsState(
                            targetValue = if (isCurrentActive) accentColor else TextSlate,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "tabIconColor"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag(tab.tag)
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    activeHoveredIndex = index
                                    CyberHapticsEngine.triggerTactileTick(context)
                                    onTabSelected(tab)
                                }
                                .padding(horizontal = 2.dp, vertical = 4.dp),
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
                                    text = tab.getLocalizedLabel(isRtl),
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = if (isCurrentActive) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.2.sp,
                                    color = if (isCurrentActive) {
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
}

/**
 * Draws a continuous C¹-smooth Liquid Glass Droplet with real volume preservation,
 * seamless squircle-to-teardrop morphing (zero discontinuities), internal caustic refraction,
 * curved specular dome lens, and ambient droplet shadow/glow.
 */
private fun DrawScope.drawRealisticLiquidGlassDroplet(
    headX: Float,
    tailX: Float,
    fluidLift: Float,
    wobble: Float,
    centerY: Float,
    maxStretchPx: Float,
    isDarkMode: Boolean,
    accentColor: Color,
    baseHeight: Float
) {
    if (headX <= 0f) return

    val signedLag = (tailX - headX).coerceIn(-maxStretchPx, maxStretchPx)
    val stretchMag = abs(signedLag)
    val stretchRatio = (stretchMag / maxStretchPx).coerceIn(0f, 1f)
    val tailSign = if (signedLag >= 0f) 1f else -1f

    val lift = fluidLift.coerceIn(0f, 1f)
    val squash = wobble.coerceIn(-0.18f, 0.18f)

    // Resting squircle vs raised spherical liquid dimensions
    val restHalfW = (baseHeight * 0.56f).coerceAtLeast(24.dp.toPx())
    val restHalfH = (baseHeight * 0.46f).coerceAtLeast(20.dp.toPx())
    val fluidR = baseHeight * 0.48f * (1f + 0.06f * lift)

    // Volume preservation: elongating horizontally narrows height subtly; landing squash widens vertically
    val volumeThinning = 1f - 0.17f * stretchRatio
    val ry = ((restHalfH * (1f - lift)) + (fluidR * lift)) * (volumeThinning + squash).coerceIn(0.68f, 1.24f)
    val leadRx = ((restHalfW * (1f - lift)) + (fluidR * lift)) * (1f - squash * 0.60f + 0.07f * stretchRatio).coerceIn(0.75f, 1.28f)

    // Dynamic meniscus trailing extension
    val tailExtension = stretchMag * 1.18f
    val tailTipRadius = (ry * (1f - 0.56f * stretchRatio)).coerceAtLeast(5.dp.toPx())

    // Unified C¹ Bezier control factor smoothly blending Squircle (0.72) <-> Liquid Dome (0.5523)
    val kCircle = 0.55228475f
    val kSquircle = 0.72f
    val dynamicMorph = maxOf(lift, stretchRatio)
    val leadK = kSquircle * (1f - dynamicMorph) + kCircle * dynamicMorph
    val tailK = leadK

    // Key contour anchors
    val frontApexX = headX - tailSign * leadRx
    val backOuterX = headX + tailSign * (leadRx + tailExtension)
    val tailCircleCenterX = backOuterX - tailSign * tailTipRadius

    // 1. Caustic Ambient Floor Shadow & Colored Refraction Halo
    val auraRadius = ry * 2.25f + tailExtension * 0.42f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                accentColor.copy(alpha = (if (isDarkMode) 0.30f else 0.20f) + 0.12f * dynamicMorph),
                accentColor.copy(alpha = 0.07f),
                Color.Transparent
            ),
            center = Offset(headX, centerY + 2.dp.toPx() * lift),
            radius = auraRadius
        ),
        radius = auraRadius,
        center = Offset(headX, centerY + 2.dp.toPx() * lift)
    )

    // 2. Single Unified Continuous Fluid Path (zero shape pops across any speed/state)
    val dropletPath = Path().apply {
        moveTo(frontApexX, centerY)

        // Front-Top Quadrant (Front Apex -> Top Crest)
        cubicTo(
            x1 = frontApexX,
            y1 = centerY - ry * leadK,
            x2 = headX - tailSign * (leadRx * leadK),
            y2 = centerY - ry,
            x3 = headX,
            y3 = centerY - ry
        )

        if (tailExtension < 0.5f) {
            // Smooth continuous resting-to-lifted superellipse symmetry when lag ~ 0
            cubicTo(
                x1 = headX + tailSign * (leadRx * tailK),
                y1 = centerY - ry,
                x2 = backOuterX,
                y2 = centerY - ry * tailK,
                x3 = backOuterX,
                y3 = centerY
            )
            cubicTo(
                x1 = backOuterX,
                y1 = centerY + ry * tailK,
                x2 = headX + tailSign * (leadRx * tailK),
                y2 = centerY + ry,
                x3 = headX,
                y3 = centerY + ry
            )
        } else {
            // Concave meniscus bridge from Top Crest to Tapered Tail Tip
            val neckBlend = stretchRatio.coerceIn(0f, 1f)
            val bridgeCp1X = headX + tailSign * (leadRx * tailK * (1f - neckBlend * 0.45f) + tailExtension * 0.28f)
            val bridgeCp2X = tailCircleCenterX - tailSign * (tailTipRadius * 0.9f + tailExtension * 0.16f)

            cubicTo(
                x1 = bridgeCp1X,
                y1 = centerY - ry,
                x2 = bridgeCp2X,
                y2 = centerY - tailTipRadius,
                x3 = tailCircleCenterX,
                y3 = centerY - tailTipRadius
            )
            // Rounded Meniscus Tail Tip Top -> Apex -> Bottom
            cubicTo(
                x1 = tailCircleCenterX + tailSign * (tailTipRadius * kCircle),
                y1 = centerY - tailTipRadius,
                x2 = backOuterX,
                y2 = centerY - tailTipRadius * kCircle,
                x3 = backOuterX,
                y3 = centerY
            )
            cubicTo(
                x1 = backOuterX,
                y1 = centerY + tailTipRadius * kCircle,
                x2 = tailCircleCenterX + tailSign * (tailTipRadius * kCircle),
                y2 = centerY + tailTipRadius,
                x3 = tailCircleCenterX,
                y3 = centerY + tailTipRadius
            )
            // Tapered Tail Tip Bottom -> Bottom Crest
            cubicTo(
                x1 = bridgeCp2X,
                y1 = centerY + tailTipRadius,
                x2 = bridgeCp1X,
                y2 = centerY + ry,
                x3 = headX,
                y3 = centerY + ry
            )
        }

        // Front-Bottom Quadrant (Bottom Crest -> Front Apex)
        cubicTo(
            x1 = headX - tailSign * (leadRx * leadK),
            y1 = centerY + ry,
            x2 = frontApexX,
            y2 = centerY + ry * leadK,
            x3 = frontApexX,
            y3 = centerY
        )
        close()
    }

    // 3. Soft Caustic Drop-Shadow Under Droplet When Lifted
    if (lift > 0.05f) {
        drawPath(
            path = dropletPath,
            color = accentColor.copy(alpha = (if (isDarkMode) 0.16f else 0.12f) * lift),
            style = Stroke(width = 4.dp.toPx() * lift, cap = StrokeCap.Round)
        )
    }

    // 4. Translucent Liquid Glass Fill with Internal Refraction
    val gradStartX = if (tailSign >= 0f) frontApexX else backOuterX
    val gradEndX = if (tailSign >= 0f) backOuterX else frontApexX

    val glassFill = if (isDarkMode) {
        Brush.linearGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.36f + 0.14f * dynamicMorph),
                Color(0xFF314159).copy(alpha = 0.44f),
                Color(0xFF1A2438).copy(alpha = 0.62f),
                accentColor.copy(alpha = 0.22f + 0.10f * stretchRatio)
            ),
            start = Offset(gradStartX, centerY - ry),
            end = Offset(gradEndX, centerY + ry)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFF0F4F9).copy(alpha = 0.88f),
                accentColor.copy(alpha = 0.22f + 0.14f * dynamicMorph),
                Color(0xFFDCE4EF).copy(alpha = 0.80f),
                accentColor.copy(alpha = 0.16f + 0.10f * stretchRatio)
            ),
            start = Offset(gradStartX, centerY - ry),
            end = Offset(gradEndX, centerY + ry)
        )
    }
    drawPath(path = dropletPath, brush = glassFill)

    // 5. Continuous Upper Crest Specular Reflection (gleams across droplet dome)
    val upperHighlightStart = headX - tailSign * (leadRx * 0.52f)
    val upperHighlightEnd = headX + tailSign * (leadRx * 0.42f + tailExtension * 0.45f)
    val highlightCurve = Path().apply {
        moveTo(upperHighlightStart, centerY - ry * 0.74f)
        cubicTo(
            x1 = headX - tailSign * (leadRx * 0.12f),
            y1 = centerY - ry * 0.88f,
            x2 = headX + tailSign * (leadRx * 0.25f + tailExtension * 0.22f),
            y2 = centerY - ry * (0.86f - 0.18f * stretchRatio),
            x3 = upperHighlightEnd,
            y3 = centerY - (ry * (1f - 0.35f * stretchRatio)) * 0.72f
        )
    }
    drawPath(
        path = highlightCurve,
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0xFFF3F6FA).copy(alpha = if (isDarkMode) 0.72f else 0.76f),
                Color.Transparent
            )
        ),
        style = Stroke(width = 1.9.dp.toPx(), cap = StrokeCap.Round)
    )

    // 6. Leading-Edge Curved Lens Specular (brightens dynamically with motion/lift)
    if (dynamicMorph > 0.08f) {
        val leadingLens = Path().apply {
            moveTo(headX - tailSign * leadRx * 0.46f, centerY - ry * 0.62f)
            cubicTo(
                x1 = frontApexX + tailSign * (leadRx * 0.15f),
                y1 = centerY - ry * 0.30f,
                x2 = frontApexX + tailSign * (leadRx * 0.15f),
                y2 = centerY + ry * 0.30f,
                x3 = headX - tailSign * leadRx * 0.46f,
                y3 = centerY + ry * 0.62f
            )
        }
        drawPath(
            path = leadingLens,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFFF3F6FA).copy(alpha = if (isDarkMode) 0.78f * dynamicMorph else 0.68f * dynamicMorph),
                    Color.Transparent
                ),
                startY = centerY - ry * 0.62f,
                endY = centerY + ry * 0.62f
            ),
            style = Stroke(width = 2.0.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // 7. Pristine Rim Refraction Border
    drawPath(
        path = dropletPath,
        brush = Brush.linearGradient(
            colors = if (isDarkMode) {
                listOf(
                    Color.White.copy(alpha = 0.68f),
                    accentColor.copy(alpha = 0.78f),
                    accentColor.copy(alpha = 0.32f),
                    Color.White.copy(alpha = 0.48f)
                )
            } else {
                listOf(
                    Color(0xFFF3F6FA).copy(alpha = 0.88f),
                    accentColor.copy(alpha = 0.54f),
                    Color(0xFFB5C3D4),
                    Color(0xFFF3F6FA).copy(alpha = 0.72f)
                )
            },
            start = Offset(gradStartX, centerY - ry),
            end = Offset(gradEndX, centerY + ry)
        ),
        style = Stroke(width = 1.25.dp.toPx(), cap = StrokeCap.Round)
    )

    // 8. Capillary Photon Glint inside Leading Dome
    if (stretchRatio > 0.18f || lift > 0.45f) {
        val glintAlpha = maxOf(
            ((stretchRatio - 0.18f) / 0.82f).coerceIn(0f, 1f),
            ((lift - 0.45f) / 0.55f).coerceIn(0f, 1f) * 0.65f
        )
        val glintPos = Offset(headX - tailSign * leadRx * 0.36f, centerY - ry * 0.26f)
        drawCircle(
            color = accentColor.copy(alpha = 0.42f * glintAlpha),
            radius = 5.5.dp.toPx(),
            center = glintPos
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.92f * glintAlpha),
            radius = 2.2.dp.toPx(),
            center = glintPos
        )
    }
}
