package pl.ultimo.mobile.feature.game.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.ultimo.mobile.ui.theme.StatusConnected
import pl.ultimo.mobile.ui.theme.StatusDisconnected
import pl.ultimo.mobile.ui.theme.StatusReconnecting
import pl.ultimo.mobile.ui.theme.SurfaceElevatedDark
import pl.ultimo.mobile.ui.theme.TextPrimary
import kotlin.math.ceil

/**
 * Radial countdown timer gauge rendering a hardware-accelerated circular progress ring.
 *
 * Dynamically shifts color from Green (ample time) -> Yellow (halfway) -> Red (critical urgency)
 * as the 30-second time bank elapses.
 */
@Composable
fun TurnCountdownTimer(
    timeRemainingMs: Long,
    totalTimeMs: Long = 30000L,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = 4.dp
) {
    val progress = (timeRemainingMs.toFloat() / totalTimeMs.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "TurnProgress"
    )

    val ringColor by animateColorAsState(
        targetValue = when {
            progress > 0.45f -> StatusConnected
            progress > 0.18f -> StatusReconnecting
            else -> StatusDisconnected
        },
        animationSpec = tween(durationMillis = 300),
        label = "TurnColor"
    )

    val secondsRemaining = ceil(timeRemainingMs / 1000.0).toInt().coerceAtLeast(0)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val arcTopLeft = Offset(strokePx / 2f, strokePx / 2f)

            // 1. Background Track
            drawArc(
                color = SurfaceElevatedDark.copy(alpha = 0.5f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokePx)
            )

            // 2. Active Progress Sweep (decreasing clockwise from top)
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        // Numerical second readout
        Text(
            text = "${secondsRemaining}s",
            style = MaterialTheme.typography.labelSmall,
            color = if (progress <= 0.18f) StatusDisconnected else TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}
