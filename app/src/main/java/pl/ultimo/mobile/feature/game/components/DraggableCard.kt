package pl.ultimo.mobile.feature.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import pl.ultimo.mobile.core.model.Card
import kotlin.math.roundToInt

/**
 * Interactive card wrapper providing physics-based 60/120 FPS dragging, elevation shadows,
 * discard pile drop-zone collision detection, spring-back recovery, and fling animations.
 *
 * All high-frequency coordinate state reads are strictly deferred to Layout and Draw phases
 * via [Modifier.offset] lambdas and [Modifier.graphicsLayer] to eliminate recompositions during gestures.
 */
@Composable
fun DraggableCard(
    card: Card,
    isPlayable: Boolean,
    modifier: Modifier = Modifier,
    initialRotation: Float = 0f,
    baseVerticalOffset: Dp = 0.dp,
    isSelected: Boolean = false,
    dropTargetBounds: Rect? = null,
    onCardPlayed: (Card) -> Unit = {},
    onCardClicked: (Card) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val baseVerticalOffsetPx = with(density) { baseVerticalOffset.toPx() }

    // Animatable motion states
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val dragTilt = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val cardAlpha = remember { Animatable(1f) }

    var isDragging by remember { mutableStateOf(false) }
    var cardBoundsInRoot by remember { mutableStateOf<Rect?>(null) }

    // Selection lift effect when card is tapped without dragging
    LaunchedEffect(isSelected, isDragging) {
        if (!isDragging) {
            val targetScale = if (isSelected) 1.08f else 1f
            scale.animateTo(
                targetValue = targetScale,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    val activeZIndex = when {
        isDragging -> 1000f
        isSelected -> 500f
        else -> 10f
    }

    Box(
        modifier = modifier
            .zIndex(activeZIndex)
            .onGloballyPositioned { coordinates ->
                cardBoundsInRoot = coordinates.boundsInRoot()
            }
            .offset {
                // Layout phase deferral: zero recompositions during drag gestures
                val selectionLiftPx = if (isSelected && !isDragging) -40f else 0f
                IntOffset(
                    x = offsetX.value.roundToInt(),
                    y = (offsetY.value + baseVerticalOffsetPx + selectionLiftPx).roundToInt()
                )
            }
            .graphicsLayer {
                // Draw phase deferral: hardware accelerated rotation, scale and elevation
                val currentRotation = if (isDragging) {
                    dragTilt.value
                } else if (isSelected) {
                    0f // Selected card straightens up for clarity
                } else {
                    initialRotation
                }
                rotationZ = currentRotation
                scaleX = scale.value
                scaleY = scale.value
                alpha = cardAlpha.value
                shadowElevation = when {
                    isDragging -> 28.dp.toPx()
                    isSelected -> 16.dp.toPx()
                    else -> 4.dp.toPx()
                }
            }
            .pointerInput(card.toWireToken(), isPlayable) {
                detectTapGestures(
                    onTap = {
                        onCardClicked(card)
                    }
                )
            }
            .pointerInput(card.toWireToken(), isPlayable) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        coroutineScope.launch {
                            scale.animateTo(
                                targetValue = 1.12f,
                                animationSpec = spring(stiffness = Spring.StiffnessHigh)
                            )
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                            // Dynamic tilt based on horizontal drag delta
                            val targetTilt = (dragAmount.x * 0.4f).coerceIn(-20f, 20f)
                            dragTilt.snapTo(targetTilt)
                        }
                    },
                    onDragEnd = {
                        val currentBounds = cardBoundsInRoot
                        val isDroppedOnTarget = evaluateDropSuccess(
                            cardBounds = currentBounds,
                            dragX = offsetX.value,
                            dragY = offsetY.value,
                            target = dropTargetBounds
                        )

                        if (isDroppedOnTarget && isPlayable) {
                            // Fling into discard pile animation
                            coroutineScope.launch {
                                launch {
                                    cardAlpha.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
                                    )
                                }
                                launch {
                                    scale.animateTo(
                                        targetValue = 0.85f,
                                        animationSpec = tween(durationMillis = 200)
                                    )
                                }
                                // Notify game controller of legal card play
                                onCardPlayed(card)

                                // Reset position for when/if this slot is reused
                                offsetX.snapTo(0f)
                                offsetY.snapTo(0f)
                                dragTilt.snapTo(0f)
                                scale.snapTo(1f)
                                cardAlpha.snapTo(1f)
                                isDragging = false
                            }
                        } else {
                            // Spring-back animation on illegal move or release outside table
                            coroutineScope.launch {
                                launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                }
                                launch {
                                    offsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                }
                                launch {
                                    dragTilt.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)
                                    )
                                }
                                launch {
                                    scale.animateTo(
                                        targetValue = 1f,
                                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                    )
                                }
                                isDragging = false
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            dragTilt.animateTo(0f)
                            scale.animateTo(1f)
                            isDragging = false
                        }
                    }
                )
            }
    ) {
        CardView(
            card = card,
            isPlayable = isPlayable,
            elevation = 0.dp // Shadow handled in graphicsLayer for 60FPS optimization
        )
    }
}

/**
 * Checks whether the card's center has entered the discard pile drop zone.
 * Falls back to vertical threshold check if target bounds are unavailable.
 */
private fun evaluateDropSuccess(
    cardBounds: Rect?,
    dragX: Float,
    dragY: Float,
    target: Rect?
): Boolean {
    if (cardBounds != null && target != null) {
        val currentCenterX = cardBounds.center.x + dragX
        val currentCenterY = cardBounds.center.y + dragY
        val inflatedTarget = target.inflate(48f)
        if (inflatedTarget.contains(Offset(currentCenterX, currentCenterY))) {
            return true
        }
    }
    // Fallback: If dragged upward by more than 160 density pixels towards the table
    return dragY < -240f
}
