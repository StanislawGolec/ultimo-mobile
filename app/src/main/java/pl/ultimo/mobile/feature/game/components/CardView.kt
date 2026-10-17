package pl.ultimo.mobile.feature.game.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor
import pl.ultimo.mobile.core.model.CardValue
import pl.ultimo.mobile.ui.theme.AccentCyan
import pl.ultimo.mobile.ui.theme.UltimoBlue
import pl.ultimo.mobile.ui.theme.UltimoBlueDark
import pl.ultimo.mobile.ui.theme.UltimoGreen
import pl.ultimo.mobile.ui.theme.UltimoGreenDark
import pl.ultimo.mobile.ui.theme.UltimoRed
import pl.ultimo.mobile.ui.theme.UltimoRedDark
import pl.ultimo.mobile.ui.theme.UltimoYellow
import pl.ultimo.mobile.ui.theme.UltimoYellowDark
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance, custom Canvas-rendered playing card component.
 *
 * Renders vector graphics for card bodies, oval centers, corner badges, and custom
 * action glyphs (SKIP, REVERSE, DRAW_TWO, and WILD 4-color quadrant dials).
 * Scales dynamically to any container dimensions with zero bitmap pixelation.
 */
@Composable
fun CardView(
    card: Card,
    modifier: Modifier = Modifier,
    isFaceDown: Boolean = false,
    isPlayable: Boolean = false,
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null
) {
    val cardShape = remember { RoundedCornerShape(10.dp) }
    val textMeasurer = rememberTextMeasurer()

    // Subtle pulsing neon glow animation for playable cards
    val infiniteTransition = rememberInfiniteTransition(label = "PlayableCardGlow")
    val glowAlpha by if (isPlayable) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "GlowAlpha"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    val cardContentDescription = remember(card, isFaceDown) {
        if (isFaceDown) "Face down card" else "Card ${card.toWireToken()}"
    }

    Box(
        modifier = modifier
            .size(width = 80.dp, height = 120.dp)
            .shadow(
                elevation = if (isPlayable) elevation + 4.dp else elevation,
                shape = cardShape,
                spotColor = if (isPlayable) AccentCyan else Color.Black
            )
            .clip(cardShape)
            .semantics { contentDescription = cardContentDescription }
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val cornerRadius = width * 0.12f

            if (isFaceDown) {
                drawFaceDownCard(width, height, cornerRadius, textMeasurer)
            } else {
                drawFaceUpCard(card, width, height, cornerRadius, textMeasurer, isPlayable, glowAlpha)
            }
        }
    }
}

/**
 * Draws the face-down back of an Ultimo card with dark felt pattern and bold logo.
 */
private fun DrawScope.drawFaceDownCard(
    width: Float,
    height: Float,
    cornerRadius: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    // Outer body
    drawRoundRect(
        color = Color(0xFF0F172A),
        size = Size(width, height),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    // Inner rim
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(width * 0.05f, height * 0.05f),
        size = Size(width * 0.9f, height * 0.9f),
        cornerRadius = CornerRadius(cornerRadius * 0.8f, cornerRadius * 0.8f),
        style = Stroke(width = width * 0.03f)
    )

    // Center tilted oval
    val ovalWidth = width * 0.65f
    val ovalHeight = height * 0.75f
    withTransform({
        rotate(degrees = -30f, pivot = center)
    }) {
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF334155), Color(0xFF0F172A)),
                center = center,
                radius = ovalHeight * 0.5f
            ),
            topLeft = Offset(center.x - ovalWidth / 2f, center.y - ovalHeight / 2f),
            size = Size(ovalWidth, ovalHeight)
        )
    }

    // Centered "ULTIMO" text
    val logoResult = textMeasurer.measure(
        text = "ULTIMO",
        style = TextStyle(
            color = UltimoRed,
            fontSize = (width * 0.17f).sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif
        )
    )
    withTransform({
        rotate(degrees = -30f, pivot = center)
    }) {
        drawText(
            textLayoutResult = logoResult,
            topLeft = Offset(
                center.x - logoResult.size.width / 2f,
                center.y - logoResult.size.height / 2f
            )
        )
    }
}

/**
 * Draws the face-up playing card with color gradients, tilted white oval, center glyph, and corner indices.
 */
private fun DrawScope.drawFaceUpCard(
    card: Card,
    width: Float,
    height: Float,
    cornerRadius: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    isPlayable: Boolean,
    glowAlpha: Float
) {
    val (primaryColor, darkColor) = card.color.toGradientColors()

    // 1. Background gradient
    val backgroundBrush = if (card.color == CardColor.WILD) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF1E293B)),
            start = Offset.Zero,
            end = Offset(width, height)
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(primaryColor, darkColor)
        )
    }

    drawRoundRect(
        brush = backgroundBrush,
        size = Size(width, height),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    // 2. Playable neon pulsing glow border or crisp white border
    if (isPlayable) {
        drawRoundRect(
            color = AccentCyan.copy(alpha = glowAlpha),
            size = Size(width, height),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = width * 0.06f)
        )
    } else {
        drawRoundRect(
            color = Color.White.copy(alpha = 0.35f),
            topLeft = Offset(width * 0.03f, height * 0.03f),
            size = Size(width * 0.94f, height * 0.94f),
            cornerRadius = CornerRadius(cornerRadius * 0.9f, cornerRadius * 0.9f),
            style = Stroke(width = width * 0.02f)
        )
    }

    // 3. Center White Oval tilted at -28 degrees
    val ovalWidth = width * 0.68f
    val ovalHeight = height * 0.72f
    withTransform({
        rotate(degrees = -28f, pivot = center)
    }) {
        drawOval(
            color = Color.White,
            topLeft = Offset(center.x - ovalWidth / 2f, center.y - ovalHeight / 2f),
            size = Size(ovalWidth, ovalHeight)
        )
    }

    // 4. Center Glyph / Number / Symbol
    drawCenterGlyph(card, primaryColor, width, height, textMeasurer)

    // 5. Corner Indices (Top-Left & Bottom-Right)
    drawCornerIndex(card, width, height, textMeasurer, isTopLeft = true)
    drawCornerIndex(card, width, height, textMeasurer, isTopLeft = false)
}

/**
 * Draws the prominent centerpiece of the card: numbers, action icons, or wild quadrants.
 */
private fun DrawScope.drawCenterGlyph(
    card: Card,
    primaryColor: Color,
    width: Float,
    height: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val glyphColor = if (card.color == CardColor.WILD) Color(0xFF0F172A) else primaryColor

    when (card.value) {
        CardValue.SKIP -> {
            // Circle-slash symbol
            val r = width * 0.22f
            val strokeW = width * 0.07f
            drawCircle(
                color = glyphColor,
                radius = r,
                center = center,
                style = Stroke(width = strokeW)
            )
            val cos45 = 0.7071f
            drawLine(
                color = glyphColor,
                start = Offset(center.x + r * cos45, center.y - r * cos45),
                end = Offset(center.x - r * cos45, center.y + r * cos45),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }

        CardValue.REVERSE -> {
            // Dual curved recycling arrows
            val r = width * 0.20f
            val strokeW = width * 0.06f
            val arcRect = Rect(center.x - r, center.y - r, center.x + r, center.y + r)

            // Top-right clockwise arc
            val topArc = Path().apply {
                arcTo(arcRect, 200f, 130f, false)
            }
            drawPath(topArc, color = glyphColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))

            // Arrow head for top arc
            val topHead = Path().apply {
                val arrowTip = Offset(center.x + r * cos(Math.toRadians(330.0).toFloat()), center.y + r * sin(Math.toRadians(330.0).toFloat()))
                moveTo(arrowTip.x, arrowTip.y)
                lineTo(arrowTip.x - width * 0.08f, arrowTip.y - height * 0.01f)
                lineTo(arrowTip.x - width * 0.02f, arrowTip.y + height * 0.05f)
                close()
            }
            drawPath(topHead, color = glyphColor, style = Fill)

            // Bottom-left counter arc
            val bottomArc = Path().apply {
                arcTo(arcRect, 20f, 130f, false)
            }
            drawPath(bottomArc, color = glyphColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))

            // Arrow head for bottom arc
            val bottomHead = Path().apply {
                val arrowTip = Offset(center.x + r * cos(Math.toRadians(150.0).toFloat()), center.y + r * sin(Math.toRadians(150.0).toFloat()))
                moveTo(arrowTip.x, arrowTip.y)
                lineTo(arrowTip.x + width * 0.08f, arrowTip.y + height * 0.01f)
                lineTo(arrowTip.x + width * 0.02f, arrowTip.y - height * 0.05f)
                close()
            }
            drawPath(bottomHead, color = glyphColor, style = Fill)
        }

        CardValue.DRAW_TWO -> {
            // Two overlapping card outlines + "+2" badge
            val miniCardW = width * 0.28f
            val miniCardH = height * 0.28f
            val cardRadius = CornerRadius(miniCardW * 0.15f, miniCardW * 0.15f)

            // Back card
            drawRoundRect(
                color = glyphColor.copy(alpha = 0.5f),
                topLeft = Offset(center.x - miniCardW * 0.8f, center.y - miniCardH * 0.6f),
                size = Size(miniCardW, miniCardH),
                cornerRadius = cardRadius,
                style = Fill
            )

            // Front card
            drawRoundRect(
                color = glyphColor,
                topLeft = Offset(center.x - miniCardW * 0.35f, center.y - miniCardH * 0.35f),
                size = Size(miniCardW, miniCardH),
                cornerRadius = cardRadius,
                style = Fill
            )

            // "+2" Text
            val textResult = textMeasurer.measure(
                text = "+2",
                style = TextStyle(
                    color = Color.White,
                    fontSize = (width * 0.20f).sp,
                    fontWeight = FontWeight.Black
                )
            )
            drawText(
                textLayoutResult = textResult,
                topLeft = Offset(
                    center.x - miniCardW * 0.35f + (miniCardW - textResult.size.width) / 2f,
                    center.y - miniCardH * 0.35f + (miniCardH - textResult.size.height) / 2f
                )
            )
        }

        CardValue.WILD -> {
            // Four-quadrant circle inside the oval
            val r = width * 0.24f
            val circleRect = Rect(center.x - r, center.y - r, center.x + r, center.y + r)

            // Top-left: Red
            drawArc(color = UltimoRed, startAngle = 180f, sweepAngle = 90f, useCenter = true, topLeft = circleRect.topLeft, size = circleRect.size)
            // Top-right: Blue
            drawArc(color = UltimoBlue, startAngle = 270f, sweepAngle = 90f, useCenter = true, topLeft = circleRect.topLeft, size = circleRect.size)
            // Bottom-right: Yellow
            drawArc(color = UltimoYellow, startAngle = 0f, sweepAngle = 90f, useCenter = true, topLeft = circleRect.topLeft, size = circleRect.size)
            // Bottom-left: Green
            drawArc(color = UltimoGreen, startAngle = 90f, sweepAngle = 90f, useCenter = true, topLeft = circleRect.topLeft, size = circleRect.size)

            // Thin cross separating quadrants
            val dividerW = width * 0.02f
            drawLine(color = Color.White, start = Offset(center.x - r, center.y), end = Offset(center.x + r, center.y), strokeWidth = dividerW)
            drawLine(color = Color.White, start = Offset(center.x, center.y - r), end = Offset(center.x, center.y + r), strokeWidth = dividerW)
        }

        CardValue.WILD_DRAW_FOUR -> {
            // Four-quadrant circle with "+4" label
            val r = width * 0.25f
            val circleRect = Rect(center.x - r, center.y - r, center.x + r, center.y + r)

            drawArc(color = UltimoRed, startAngle = 180f, sweepAngle = 90f, useCenter = true, topLeft = circleRect.topLeft, size = circleRect.size)
            drawArc(color = UltimoBlue, startAngle = 270f, sweepAngle = 90f, useCenter = true, topLeft = circleRect.topLeft, size = circleRect.size)
            drawArc(color = UltimoYellow, startAngle = 0f, sweepAngle = 90f, useCenter = true, topLeft = circleRect.topLeft, size = circleRect.size)
            drawArc(color = UltimoGreen, startAngle = 90f, sweepAngle = 90f, useCenter = true, topLeft = circleRect.topLeft, size = circleRect.size)

            // Center small white disk with bold "+4"
            val centerDiskR = r * 0.65f
            drawCircle(color = Color.White, radius = centerDiskR, center = center)

            val textResult = textMeasurer.measure(
                text = "+4",
                style = TextStyle(
                    color = Color(0xFF0F172A),
                    fontSize = (width * 0.22f).sp,
                    fontWeight = FontWeight.Black
                )
            )
            drawText(
                textLayoutResult = textResult,
                topLeft = Offset(center.x - textResult.size.width / 2f, center.y - textResult.size.height / 2f)
            )
        }

        else -> {
            // Standard Digits 0 through 9
            val digitText = card.value.pointValue.toString()
            val textResult = textMeasurer.measure(
                text = digitText,
                style = TextStyle(
                    color = glyphColor,
                    fontSize = (width * 0.44f).sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif
                )
            )

            // Shadow under the digit for visual depth
            drawText(
                textLayoutResult = textResult,
                topLeft = Offset(
                    center.x - textResult.size.width / 2f + width * 0.015f,
                    center.y - textResult.size.height / 2f + height * 0.01f
                )
            )

            // Draw primary digit
            drawText(
                textLayoutResult = textResult,
                topLeft = Offset(
                    center.x - textResult.size.width / 2f,
                    center.y - textResult.size.height / 2f
                )
            )

            // Underline for 6 and 9 to prevent confusion
            if (card.value == CardValue.SIX || card.value == CardValue.NINE) {
                val underlineY = center.y + textResult.size.height * 0.45f
                val underlineW = textResult.size.width * 0.8f
                drawLine(
                    color = glyphColor,
                    start = Offset(center.x - underlineW / 2f, underlineY),
                    end = Offset(center.x + underlineW / 2f, underlineY),
                    strokeWidth = width * 0.035f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Draws the corner index token (e.g. "7", "⊘", "+2", "W") on the top-left or inverted bottom-right.
 */
private fun DrawScope.drawCornerIndex(
    card: Card,
    width: Float,
    height: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    isTopLeft: Boolean
) {
    val tokenText = when (card.value) {
        CardValue.SKIP -> "⊘"
        CardValue.REVERSE -> "⇄"
        CardValue.DRAW_TWO -> "+2"
        CardValue.WILD -> "★"
        CardValue.WILD_DRAW_FOUR -> "+4"
        else -> card.value.pointValue.toString()
    }

    val indexResult = textMeasurer.measure(
        text = tokenText,
        style = TextStyle(
            color = Color.White,
            fontSize = (width * 0.16f).sp,
            fontWeight = FontWeight.Black
        )
    )

    val marginX = width * 0.08f
    val marginY = height * 0.06f

    if (isTopLeft) {
        drawText(
            textLayoutResult = indexResult,
            topLeft = Offset(marginX, marginY)
        )
    } else {
        withTransform({
            rotate(degrees = 180f, pivot = Offset(width - marginX - indexResult.size.width / 2f, height - marginY - indexResult.size.height / 2f))
        }) {
            drawText(
                textLayoutResult = indexResult,
                topLeft = Offset(width - marginX - indexResult.size.width, height - marginY - indexResult.size.height)
            )
        }
    }
}

/**
 * Resolves color gradients for standard cards.
 */
private fun CardColor.toGradientColors(): Pair<Color, Color> {
    return when (this) {
        CardColor.RED -> Pair(UltimoRed, UltimoRedDark)
        CardColor.BLUE -> Pair(UltimoBlue, UltimoBlueDark)
        CardColor.GREEN -> Pair(UltimoGreen, UltimoGreenDark)
        CardColor.YELLOW -> Pair(UltimoYellow, UltimoYellowDark)
        CardColor.WILD -> Pair(Color(0xFF334155), Color(0xFF0F172A))
    }
}
