package com.example.ui.components

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class NavigationTab(val label: String, val arabicLabel: String, val icon: ImageVector, val tag: String) {
    BATTERY("Battery", "البطارية", Icons.Default.Bolt, "nav_battery"),
    BATTERY_SOH("Cell SOH", "صحة الخلية", Icons.Default.HealthAndSafety, "nav_soh"),
    CABLE_DNA("Cable DNA", "كابل DNA", Icons.Default.Cable, "nav_cable_dna"),
    MY_SPACE("My Space", "مساحتي", Icons.Default.Tune, "nav_my_space");

    fun getLocalizedLabel(isArabic: Boolean): String = if (isArabic) arabicLabel else label
}

/**
 * Apple Liquid Glass Floating Navigation Bar with Dynamic Drag Droplet Effect.
 *
 * Continuous drag gesture ("من دون نزع يدي"):
 * - Swiping right-to-left: Droplet head moves left with trailing tail extending to the right.
 * - Swiping left-to-right: Droplet head moves right with trailing tail inverting to the left.
 * - Real-time tracking across tabs, haptic ticks on boundary crossing, and spring snap on release.
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
    val coroutineScope = rememberCoroutineScope()

    val barBackground = if (isDarkMode) Color(0xF01E293B) else Color(0xEBFFFFFF)
    val shadowColor = if (isDarkMode) Color(0x66000000) else Color(0x2B9AAEC4)
    val borderColor = if (isDarkMode) Color(0x33475569) else Color(0x66E2E8F0)

    val maxStretchPx = with(density) { 44.dp.toPx() }
    val baseStretchPx = with(density) { 14.dp.toPx() }
    val snapStretchPx = with(density) { 26.dp.toPx() }

    // Droplet physics state
    val dropletX = remember { Animatable(0f) }
    val tailStretch = remember { Animatable(0f) }
    // tailDirection: +1f for tail pointing Right (finger moving Left), -1f for tail pointing Left (finger moving Right)
    val tailDirection = remember { Animatable(0f) }

    var isDragging by remember { mutableStateOf(false) }
    var activeHoveredIndex by remember { mutableIntStateOf(selectedTab.ordinal) }
    var hasInitialized by remember { mutableStateOf(false) }

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
                    // Dual Soft Neumorphic Drop Shadow
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
                    width = 1.2.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            val barWidthPx = constraints.maxWidth.toFloat()
            val totalTabs = NavigationTab.entries.size
            val tabWidthPx = if (totalTabs > 0) barWidthPx / totalTabs else 1f

            fun tabCenterX(index: Int): Float = (index + 0.5f) * tabWidthPx

            // Initialize droplet position on first layout
            if (!hasInitialized && barWidthPx > 0f) {
                LaunchedEffect(barWidthPx) {
                    val initialCenter = tabCenterX(selectedTab.ordinal)
                    dropletX.snapTo(initialCenter)
                    activeHoveredIndex = selectedTab.ordinal
                    hasInitialized = true
                }
            }

    // External selection sync (when user changes tab outside or taps directly with a single click)
    LaunchedEffect(selectedTab, hasInitialized) {
        if (hasInitialized && !isDragging) {
            val targetX = tabCenterX(selectedTab.ordinal)
            val currentX = dropletX.value
            val diff = targetX - currentX
            activeHoveredIndex = selectedTab.ordinal

            if (abs(diff) > 2f) {
                coroutineScope.launch {
                    // Single tap transition: strictly WITHOUT TAIL (tailStretch = 0f)
                    // The squircle / rounded square glides smoothly to the selected tab with zero tail formation
                    launch {
                        tailStretch.snapTo(0f)
                    }
                    // Liquid squircle glide with organic spring physics
                    dropletX.animateTo(
                        targetValue = targetX,
                        animationSpec = spring(
                            dampingRatio = 0.76f,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                }
            }
        }
    }

            // Interactive Continuous Pointer Gesture Handler ("من دون نزع يدي")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(barWidthPx) {
                        coroutineScope {
                            while (isActive) {
                                awaitPointerEventScope {
                                    val down = awaitFirstDown(
                                        requireUnconsumed = false,
                                        pass = PointerEventPass.Initial
                                    )
                                    isDragging = true

                                    var lastX = down.position.x
                                    var lastTime = down.uptimeMillis
                                    val startX = down.position.x

                                    // When pressed down:
                                    // The droplet forms as a pure round liquid lens WITHOUT A TAIL!
                                    launch {
                                        tailStretch.snapTo(0f)
                                    }

                                    val initialIndex = (startX / tabWidthPx).toInt().coerceIn(0, totalTabs - 1)
                                    activeHoveredIndex = initialIndex

                                    // Follow finger position directly
                                    val initialClampedX = startX.coerceIn(tabWidthPx * 0.35f, barWidthPx - tabWidthPx * 0.35f)
                                    launch {
                                        dropletX.snapTo(initialClampedX)
                                    }

                                    var smoothedVelocity = 0f

                                    // Continuous dragging loop without lifting finger
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                        if (!change.pressed) {
                                            // Finger released
                                            break
                                        }

                                        val currentTime = change.uptimeMillis
                                        val currentX = change.position.x
                                        val dt = (currentTime - lastTime).coerceAtLeast(1L)
                                        val dx = currentX - lastX

                                        lastX = currentX
                                        lastTime = currentTime

                                        // Calculate instantaneous and smoothed velocity (px/sec)
                                        val instantVelocity = (dx / dt.toFloat()) * 1000f
                                        smoothedVelocity = (smoothedVelocity * 0.45f) + (instantVelocity * 0.55f)
                                        val speed = abs(smoothedVelocity)

                                        // 1:1 true finger tracking - head follows finger position directly
                                        val clampedX = currentX.coerceIn(tabWidthPx * 0.35f, barWidthPx - tabWidthPx * 0.35f)
                                        launch {
                                            dropletX.snapTo(clampedX)
                                        }

                                        // Dynamic Tail Physics based on finger velocity:
                                        // When stationary or moving very slowly (< 40 px/s): stretch = 0 (NO TAIL!)
                                        // As finger moves faster: tail elongates up to maxStretchPx!
                                        // As finger slows down: tail shortens and retracts!
                                        val targetStretch = if (speed < 40f) {
                                            0f
                                        } else {
                                            ((speed - 40f) / 1600f * maxStretchPx).coerceIn(0f, maxStretchPx)
                                        }

                                        // Direction of tail:
                                        // Moving Left (smoothedVelocity < 0) -> tail points Right (+1f)
                                        // Moving Right (smoothedVelocity > 0) -> tail points Left (-1f)
                                        if (abs(smoothedVelocity) > 25f) {
                                            val dir = if (smoothedVelocity < 0f) 1f else -1f
                                            launch {
                                                tailDirection.snapTo(dir)
                                            }
                                        }

                                        launch {
                                            tailStretch.animateTo(
                                                targetValue = targetStretch,
                                                animationSpec = tween(
                                                    durationMillis = if (targetStretch < tailStretch.value) 75 else 35,
                                                    easing = LinearEasing
                                                )
                                            )
                                        }

                                        // Tab boundary transition
                                        val hoveredIndex = (clampedX / tabWidthPx).toInt().coerceIn(0, totalTabs - 1)
                                        if (hoveredIndex != activeHoveredIndex) {
                                            activeHoveredIndex = hoveredIndex
                                            CyberHapticsEngine.triggerTactileTick(context)
                                            onTabSelected(NavigationTab.entries[hoveredIndex])
                                        }

                                        change.consume()
                                    }

                                    // Finger up: snap to selected tab
                                    isDragging = false
                                    val finalIndex = activeHoveredIndex
                                    val finalTab = NavigationTab.entries[finalIndex]
                                    val snapTargetX = tabCenterX(finalIndex)

                                    CyberHapticsEngine.triggerTactileTick(context)
                                    onTabSelected(finalTab)

                                    // Collapse tail to 0 and spring into tab center
                                    launch {
                                        tailStretch.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                    }
                                    launch {
                                        dropletX.animateTo(
                                            targetValue = snapTargetX,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioLowBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
            ) {
                // Layer 1: Apple Liquid Glass Droplet Canvas (underneath tabs)
                if (showLiquidDroplet) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawAppleLiquidGlassDroplet(
                            currentX = dropletX.value,
                            centerY = size.height / 2f,
                            stretch = tailStretch.value,
                            tailDir = tailDirection.value,
                            isDragging = isDragging,
                            isDarkMode = isDarkMode,
                            accentColor = accentColor,
                            baseWidth = tabWidthPx * 0.88f,
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
                                val isRtl = androidx.compose.ui.platform.LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl
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
 * Draws the luxury Apple Liquid Glass Droplet with dynamic trailing tail,
 * internal glass refraction, specular highlight lens, and ambient glowing aura.
 */
private fun DrawScope.drawAppleLiquidGlassDroplet(
    currentX: Float,
    centerY: Float,
    stretch: Float,
    tailDir: Float,
    isDragging: Boolean,
    isDarkMode: Boolean,
    accentColor: Color,
    baseWidth: Float,
    baseHeight: Float
) {
    if (currentX <= 0f) return

    val headRadius = baseHeight * 0.48f
    val cornerRadius = 18.dp.toPx()

    // 1. Ambient Glow Aura Tint
    val auraRadius = headRadius * 2.3f + stretch * 0.5f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                accentColor.copy(alpha = if (isDarkMode) 0.32f else 0.22f),
                accentColor.copy(alpha = 0.08f),
                Color.Transparent
            ),
            center = Offset(currentX, centerY),
            radius = auraRadius
        ),
        radius = auraRadius,
        center = Offset(currentX, centerY)
    )

    // Continuous organic fluid morphing:
    // Let R be the base radius of the droplet.
    // When stationary (stretch == 0), the contour is mathematically identical to a circle (or rounded resting pill).
    // As stretch increases with finger speed, the trailing tail continuously pulls out while preserving fluid volume (waist thinning).
    val R = headRadius
    val tailSign = if (tailDir >= 0f) 1f else -1f

    // Organic stretch ratio [0..1]
    val stretchRatio = (stretch / with(this) { 44.dp.toPx() }).coerceIn(0f, 1f)
    val tailLength = stretch * 1.45f

    // Fluid volume preservation: minor waist thinning perpendicular to motion
    val thinning = R * (0.14f * stretchRatio)
    val Ry = (R - thinning).coerceAtLeast(R * 0.72f)

    // Tail tip thickness continuously tapers from R down to a fluid meniscus tip as stretch increases
    val minTipRadius = with(this) { 4.5.dp.toPx() }
    val tailTipRadius = R - (R - minTipRadius) * stretchRatio

    // Resting squircle geometry (مربع مستدير الحواف عند عدم اللمس)
    // Dimension is approximately 48dp x 44dp with 14dp rounded corners, perfectly framing the active icon & label
    val squircleHalfWidth = (baseHeight * 0.54f).coerceAtLeast(22.dp.toPx())
    val squircleHalfHeight = (baseHeight * 0.46f).coerceAtLeast(20.dp.toPx())
    val squircleCornerRadius = 14.dp.toPx()

    // Smooth transition from squircle to moving droplet
    val isRestingSquircle = !isDragging && stretch <= 0.8f

    val activePath = Path().apply {
        if (isRestingSquircle) {
            // Pristine Apple Liquid Glass Rounded Square (مربع مستدير الحواف)
            addRoundRect(
                RoundRect(
                    rect = Rect(
                        left = currentX - squircleHalfWidth,
                        top = centerY - squircleHalfHeight,
                        right = currentX + squircleHalfWidth,
                        bottom = centerY + squircleHalfHeight
                    ),
                    cornerRadius = CornerRadius(squircleCornerRadius, squircleCornerRadius)
                )
            )
        } else {
            // Dynamic Viscous Liquid Droplet with realistic fluid physics & meniscus tail
            val morphFactor = stretchRatio.coerceIn(0f, 1f)
            val baseLeadRadius = squircleHalfWidth * (1f - morphFactor) + R * morphFactor
            val leadExt = baseLeadRadius + (thinning * 0.25f)
            val dynamicLeadingX = currentX - (tailSign * leadExt)
            val dynamicTailTipX = currentX + (tailSign * (baseLeadRadius + tailLength))

            val kCircle = 0.55228475f

            moveTo(dynamicLeadingX, centerY)

            // Segment 1: Leading apex -> Top crest
            cubicTo(
                x1 = dynamicLeadingX,
                y1 = centerY - Ry * kCircle,
                x2 = currentX - tailSign * (R * kCircle),
                y2 = centerY - Ry,
                x3 = currentX,
                y3 = centerY - Ry
            )

            // Segment 2: Top crest -> Tail tip top
            val cp1X = currentX + tailSign * (R * kCircle * (1f - stretchRatio * 0.6f) + tailLength * 0.25f)
            val cp1Y = centerY - Ry
            val cp2X = dynamicTailTipX - tailSign * (tailTipRadius * 1.1f)
            val cp2Y = centerY - tailTipRadius
            val tipTopX = dynamicTailTipX
            val tipTopY = centerY - tailTipRadius

            cubicTo(
                x1 = cp1X,
                y1 = cp1Y,
                x2 = cp2X,
                y2 = cp2Y,
                x3 = tipTopX,
                y3 = tipTopY
            )

            // Segment 3: Round the tail tip continuously
            cubicTo(
                x1 = dynamicTailTipX + tailSign * tailTipRadius * kCircle,
                y1 = centerY - tailTipRadius,
                x2 = dynamicTailTipX + tailSign * tailTipRadius * kCircle,
                y2 = centerY + tailTipRadius,
                x3 = dynamicTailTipX,
                y3 = centerY + tailTipRadius
            )

            // Segment 4: Tail tip bottom -> Bottom crest
            val cp3X = dynamicTailTipX - tailSign * (tailTipRadius * 1.1f)
            val cp3Y = centerY + tailTipRadius
            val cp4X = currentX + tailSign * (R * kCircle * (1f - stretchRatio * 0.6f) + tailLength * 0.25f)
            val cp4Y = centerY + Ry

            cubicTo(
                x1 = cp3X,
                y1 = cp3Y,
                x2 = cp4X,
                y2 = cp4Y,
                x3 = currentX,
                y3 = centerY + Ry
            )

            // Segment 5: Bottom crest -> Leading apex
            cubicTo(
                x1 = currentX - tailSign * (R * kCircle),
                y1 = centerY + Ry,
                x2 = dynamicLeadingX,
                y2 = centerY + Ry * kCircle,
                x3 = dynamicLeadingX,
                y3 = centerY
            )
            close()
        }
    }

    val leadingX = if (isRestingSquircle) currentX - squircleHalfWidth else currentX - (tailSign * (R + tailLength * 0.2f))
    val tailTipX = if (isRestingSquircle) currentX + squircleHalfWidth else currentX + (tailSign * (R + tailLength))

    // 2. Apple Liquid Glass Translucent Body (with dynamic internal refraction)
    val startGradientX = if (tailSign >= 0) leadingX else tailTipX
    val endGradientX = if (tailSign >= 0) tailTipX else leadingX

    val glassFill = if (isDarkMode) {
        Brush.linearGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.38f + 0.10f * stretchRatio),
                Color(0xFF334155).copy(alpha = 0.45f),
                Color(0xFF1E293B).copy(alpha = 0.65f),
                accentColor.copy(alpha = 0.22f + 0.08f * stretchRatio)
            ),
            start = Offset(startGradientX, centerY - Ry),
            end = Offset(endGradientX, centerY + Ry)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.90f),
                accentColor.copy(alpha = 0.24f + 0.12f * stretchRatio),
                Color.White.copy(alpha = 0.65f),
                accentColor.copy(alpha = 0.16f + 0.10f * stretchRatio)
            ),
            start = Offset(startGradientX, centerY - Ry),
            end = Offset(endGradientX, centerY + Ry)
        )
    }

    drawPath(path = activePath, brush = glassFill)

    // 3. Specular Lens Highlight
    if (isRestingSquircle) {
        // Luxury Top Glass Arc for Squircle
        val squircleShimmer = Path().apply {
            moveTo(currentX - squircleHalfWidth * 0.7f, centerY - squircleHalfHeight + 3.dp.toPx())
            lineTo(currentX + squircleHalfWidth * 0.7f, centerY - squircleHalfHeight + 3.dp.toPx())
        }
        drawPath(
            path = squircleShimmer,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = if (isDarkMode) 0.65f else 0.85f),
                    Color.Transparent
                )
            ),
            style = Stroke(width = with(this) { 1.8.dp.toPx() }, cap = StrokeCap.Round)
        )
    } else {
        // Continuous Leading Edge Dome Specular Lens Highlight for Droplet
        val leadingSpecular = Path().apply {
            moveTo(currentX - tailSign * R * 0.45f, centerY - Ry * 0.68f)
            cubicTo(
                x1 = leadingX + tailSign * (R * 0.12f),
                y1 = centerY - Ry * 0.36f,
                x2 = leadingX + tailSign * (R * 0.12f),
                y2 = centerY + Ry * 0.36f,
                x3 = currentX - tailSign * R * 0.45f,
                y3 = centerY + Ry * 0.68f
            )
        }
        drawPath(
            path = leadingSpecular,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.15f),
                    Color.White.copy(alpha = 0.88f),
                    Color.White.copy(alpha = 0.15f)
                ),
                startY = centerY - Ry * 0.68f,
                endY = centerY + Ry * 0.68f
            ),
            style = Stroke(width = with(this) { 2.2.dp.toPx() }, cap = StrokeCap.Round)
        )
    }

    // 4. Top Crest Specular Shimmer (Gleams across the upper curvature when moving)
    if (!isRestingSquircle) {
        val topSpecular = Path().apply {
            moveTo(currentX - tailSign * R * 0.5f, centerY - Ry * 0.72f)
            cubicTo(
                x1 = currentX,
                y1 = centerY - Ry * 0.94f,
                x2 = currentX + tailSign * (R * 0.4f + tailLength * 0.35f),
                y2 = centerY - (Ry * 0.90f - thinning * 0.4f),
                x3 = currentX + tailSign * (R * 0.75f + tailLength * 0.60f),
                y3 = centerY - (tailTipRadius * 1.15f)
            )
        }
        drawPath(
            path = topSpecular,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = if (isDarkMode) 0.65f else 0.85f),
                    Color.Transparent
                )
            ),
            style = Stroke(width = with(this) { 1.6.dp.toPx() }, cap = StrokeCap.Round)
        )
    }

    // 5. Outer Liquid Glass Rim Border with Pristine Refraction
    drawPath(
        path = activePath,
        brush = Brush.linearGradient(
            colors = if (isDarkMode) {
                listOf(
                    Color.White.copy(alpha = 0.68f),
                    accentColor.copy(alpha = 0.75f),
                    accentColor.copy(alpha = 0.30f),
                    Color.White.copy(alpha = 0.45f)
                )
            } else {
                listOf(
                    Color.White.copy(alpha = 0.95f),
                    accentColor.copy(alpha = 0.55f),
                    Color(0xFFCBD5E1),
                    Color.White.copy(alpha = 0.75f)
                )
            },
            start = Offset(startGradientX, centerY - Ry),
            end = Offset(endGradientX, centerY + Ry)
        ),
        style = Stroke(width = with(this) { 1.2.dp.toPx() }, cap = StrokeCap.Round)
    )

    // 6. Dynamic Internal Photon Glint (appears progressively with fluid velocity)
    if (stretchRatio > 0.25f) {
        val glintAlpha = ((stretchRatio - 0.25f) / 0.75f).coerceIn(0f, 1f)
        drawCircle(
            color = Color.White.copy(alpha = 0.90f * glintAlpha),
            radius = with(this) { 2.4.dp.toPx() },
            center = Offset(currentX - tailSign * R * 0.35f, centerY - Ry * 0.25f)
        )
        drawCircle(
            color = accentColor.copy(alpha = 0.45f * glintAlpha),
            radius = with(this) { 6.dp.toPx() },
            center = Offset(currentX - tailSign * R * 0.35f, centerY - Ry * 0.25f)
        )
    }
}
