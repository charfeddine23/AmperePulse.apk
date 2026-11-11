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

enum class NavigationTab(val label: String, val icon: ImageVector, val tag: String) {
    BATTERY("Battery", Icons.Default.Bolt, "nav_battery"),
    STATISTICS("Stats", Icons.Default.QueryStats, "nav_statistics"),
    BATTERY_SOH("Cell SOH", Icons.Default.HealthAndSafety, "nav_soh"),
    CABLE_DNA("Cable DNA", Icons.Default.Cable, "nav_cable_dna"),
    MY_SPACE("My Space", Icons.Default.Tune, "nav_my_space")
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
    accentColor: Color = TactileRoyalBlue
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

            // External selection sync (when user changes tab outside or taps directly)
            LaunchedEffect(selectedTab, hasInitialized) {
                if (hasInitialized && !isDragging) {
                    val targetX = tabCenterX(selectedTab.ordinal)
                    val currentX = dropletX.value
                    val diff = targetX - currentX
                    activeHoveredIndex = selectedTab.ordinal

                    if (abs(diff) > 2f) {
                        // Moving Left (target < current) -> tail points Right (+1f)
                        // Moving Right (target > current) -> tail points Left (-1f)
                        val dir = if (diff < 0) 1f else -1f
                        coroutineScope.launch {
                            tailDirection.snapTo(dir)
                            launch {
                                tailStretch.animateTo(
                                    targetValue = snapStretchPx,
                                    animationSpec = tween(120)
                                )
                                tailStretch.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                            dropletX.animateTo(
                                targetValue = targetX,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
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
                                    text = tab.label,
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

    // Construct droplet geometry: resting capsule vs. stretched dynamic teardrop vs. round pressed drop
    val activePath: Path
    val leadingX: Float
    val tailTipX: Float
    val tailSign = if (tailDir >= 0f) 1f else -1f

    if (stretch <= 2.dp.toPx()) {
        if (isDragging) {
            // When pressed or stationary while holding finger: Pure circular liquid glass droplet WITHOUT TAIL!
            val diam = headRadius * 2f
            val left = currentX - headRadius
            val top = centerY - headRadius
            leadingX = left
            tailTipX = left + diam

            activePath = Path().apply {
                addOval(Rect(left, top, left + diam, top + diam))
            }
        } else {
            // Resting Liquid Glass Pill when settled on active tab
            val left = currentX - baseWidth * 0.44f
            val top = centerY - baseHeight * 0.44f
            val w = baseWidth * 0.88f
            val h = baseHeight * 0.88f

            leadingX = left
            tailTipX = left + w

            activePath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(left, top, left + w, top + h),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                )
            }
        }
    } else {
        // Dynamic Liquid Teardrop: tail forms, elongates, and shortens strictly with velocity!
        val tailLength = stretch * 1.35f
        leadingX = currentX - (tailSign * headRadius)
        tailTipX = currentX + (tailSign * (headRadius + tailLength))
        val tailRadius = (headRadius * 0.28f).coerceAtLeast(3.5.dp.toPx())

        activePath = Path().apply {
            moveTo(leadingX, centerY)

            // Curve from leading edge to top of head
            cubicTo(
                x1 = leadingX,
                y1 = centerY - headRadius * 0.55f,
                x2 = currentX - tailSign * headRadius * 0.55f,
                y2 = centerY - headRadius,
                x3 = currentX,
                y3 = centerY - headRadius
            )

            // Curve from top of head to top of tail tip
            cubicTo(
                x1 = currentX + tailSign * tailLength * 0.30f,
                y1 = centerY - headRadius * 0.95f,
                x2 = currentX + tailSign * tailLength * 0.70f,
                y2 = centerY - tailRadius * 1.25f,
                x3 = tailTipX,
                y3 = centerY - tailRadius
            )

            // Round the tail tip
            cubicTo(
                x1 = tailTipX + tailSign * tailRadius * 0.55f,
                y1 = centerY - tailRadius,
                x2 = tailTipX + tailSign * tailRadius * 0.55f,
                y2 = centerY + tailRadius,
                x3 = tailTipX,
                y3 = centerY + tailRadius
            )

            // Curve from bottom of tail tip back to bottom of head
            cubicTo(
                x1 = currentX + tailSign * tailLength * 0.70f,
                y1 = centerY + tailRadius * 1.25f,
                x2 = currentX + tailSign * tailLength * 0.30f,
                y2 = centerY + headRadius * 0.95f,
                x3 = currentX,
                y3 = centerY + headRadius
            )

            // Curve from bottom of head back to leading edge
            cubicTo(
                x1 = currentX - tailSign * headRadius * 0.55f,
                y1 = centerY + headRadius,
                x2 = leadingX,
                y2 = centerY + headRadius * 0.55f,
                x3 = leadingX,
                y3 = centerY
            )
            close()
        }
    }

    // 2. Liquid Glass Body (Translucent refractive sheen)
    val startGradientX = if (tailSign >= 0) leadingX else tailTipX
    val endGradientX = if (tailSign >= 0) tailTipX else leadingX

    val glassFill = if (isDarkMode) {
        Brush.linearGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.38f),
                Color(0xFF334155).copy(alpha = 0.45f),
                Color(0xFF1E293B).copy(alpha = 0.65f),
                accentColor.copy(alpha = 0.22f)
            ),
            start = Offset(startGradientX, centerY - headRadius),
            end = Offset(endGradientX, centerY + headRadius)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.90f),
                accentColor.copy(alpha = 0.24f),
                Color.White.copy(alpha = 0.65f),
                accentColor.copy(alpha = 0.16f)
            ),
            start = Offset(startGradientX, centerY - headRadius),
            end = Offset(endGradientX, centerY + headRadius)
        )
    }

    drawPath(path = activePath, brush = glassFill)

    // 3. Specular Highlight for Pure Round Droplet (when pressed with zero tail)
    if (isDragging && stretch <= 2.dp.toPx()) {
        val roundLensHighlight = Path().apply {
            moveTo(currentX - headRadius * 0.55f, centerY - headRadius * 0.25f)
            cubicTo(
                x1 = currentX - headRadius * 0.45f,
                y1 = centerY - headRadius * 0.70f,
                x2 = currentX + headRadius * 0.20f,
                y2 = centerY - headRadius * 0.75f,
                x3 = currentX + headRadius * 0.50f,
                y3 = centerY - headRadius * 0.35f
            )
        }
        drawPath(
            path = roundLensHighlight,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = if (isDarkMode) 0.65f else 0.85f),
                    Color.Transparent
                )
            ),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // 3b. Leading Edge Specular Highlight (Liquid Glass Front Lens when moving)
    if (stretch > 2.dp.toPx()) {
        val leadingSpecular = Path().apply {
            moveTo(currentX - tailSign * headRadius * 0.5f, centerY - headRadius * 0.7f)
            cubicTo(
                x1 = leadingX,
                y1 = centerY - headRadius * 0.35f,
                x2 = leadingX,
                y2 = centerY + headRadius * 0.35f,
                x3 = currentX - tailSign * headRadius * 0.5f,
                y3 = centerY + headRadius * 0.7f
            )
        }
        drawPath(
            path = leadingSpecular,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.20f),
                    Color.White.copy(alpha = 0.88f),
                    Color.White.copy(alpha = 0.20f)
                ),
                startY = centerY - headRadius * 0.7f,
                endY = centerY + headRadius * 0.7f
            ),
            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // 4. Top Crest Specular Shimmer
    val topSpecular = Path().apply {
        moveTo(currentX - tailSign * headRadius * 0.45f, centerY - headRadius * 0.75f)
        cubicTo(
            x1 = currentX,
            y1 = centerY - headRadius * 0.92f,
            x2 = currentX + tailSign * headRadius * 0.35f,
            y2 = centerY - headRadius * 0.88f,
            x3 = currentX + tailSign * headRadius * 0.65f,
            y3 = centerY - headRadius * 0.70f
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
        style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
    )

    // 5. Outer Liquid Glass Rim Border
    drawPath(
        path = activePath,
        brush = Brush.linearGradient(
            colors = if (isDarkMode) {
                listOf(
                    Color.White.copy(alpha = 0.65f),
                    accentColor.copy(alpha = 0.75f),
                    accentColor.copy(alpha = 0.25f),
                    Color.White.copy(alpha = 0.40f)
                )
            } else {
                listOf(
                    Color.White.copy(alpha = 0.95f),
                    accentColor.copy(alpha = 0.50f),
                    Color(0xFFCBD5E1),
                    Color.White.copy(alpha = 0.75f)
                )
            },
            start = Offset(startGradientX, centerY - headRadius),
            end = Offset(endGradientX, centerY + headRadius)
        ),
        style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
    )

    // 6. Dynamic Photon Glint when moving fast
    if (stretch > 14.dp.toPx()) {
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = 2.5.dp.toPx(),
            center = Offset(currentX - tailSign * headRadius * 0.35f, centerY - headRadius * 0.25f)
        )
        drawCircle(
            color = accentColor.copy(alpha = 0.45f),
            radius = 6.dp.toPx(),
            center = Offset(currentX - tailSign * headRadius * 0.35f, centerY - headRadius * 0.25f)
        )
    }
}
