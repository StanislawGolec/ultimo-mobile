package pl.ultimo.mobile.feature.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.ultimo.mobile.ui.theme.AccentCyan
import pl.ultimo.mobile.ui.theme.TextPrimary
import pl.ultimo.mobile.ui.theme.UltimoRed
import pl.ultimo.mobile.ui.theme.UltimoYellow

/**
 * Tactical defense HUD banner displayed at the top/center of the game table.
 *
 * Provides real-time visual alerts for:
 * 1. Skip Deflection: Prompting player to play a SKIP card to deflect block onto the next player.
 * 2. Penalty Stacking: Alerting accumulated draw penalties (+2, +4, +6...) with quick option to defend or draw.
 * 3. Auto-Pass Notice: Transient advisory when turn has been auto-passed due to timer expiry.
 */
@Composable
fun DefenseHudBanner(
    pendingSkipDeflection: Boolean,
    pendingDrawPenalty: Int,
    autoPassNotice: String?,
    modifier: Modifier = Modifier,
    onTakePenalty: () -> Unit = {},
    onDismissAutoPass: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "DefensePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Skip Deflection Shield Banner
        AnimatedVisibility(
            visible = pendingSkipDeflection,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                AccentCyan.copy(alpha = 0.35f * pulseAlpha),
                                Color(0xFF003B46).copy(alpha = 0.85f),
                                AccentCyan.copy(alpha = 0.35f * pulseAlpha)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        color = AccentCyan.copy(alpha = pulseAlpha),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("defense_skip_banner"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Deflection Shield",
                        tint = AccentCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "DEFEND: Play a SKIP card to deflect block to next player!",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // 2. Penalty Stacking Warning Banner
        AnimatedVisibility(
            visible = pendingDrawPenalty > 0,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                UltimoRed.copy(alpha = 0.4f * pulseAlpha),
                                Color(0xFF4A0E17).copy(alpha = 0.9f),
                                UltimoYellow.copy(alpha = 0.35f * pulseAlpha)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        color = UltimoRed.copy(alpha = pulseAlpha),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(onClick = onTakePenalty)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("defense_penalty_banner"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Penalty Warning",
                        tint = UltimoYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "PENALTY STACK: +$pendingDrawPenalty CARDS! Play a Draw card to defend or tap DRAW to take penalty.",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // 3. Auto-Pass Notice Banner
        AnimatedVisibility(
            visible = !autoPassNotice.isNullOrBlank(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF334155).copy(alpha = 0.9f))
                    .border(1.dp, UltimoYellow.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .clickable(onClick = onDismissAutoPass)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("defense_auto_pass_banner"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = autoPassNotice ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = UltimoYellow,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
