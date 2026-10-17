package pl.ultimo.mobile.feature.game.dialog

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pl.ultimo.mobile.core.model.CardColor
import pl.ultimo.mobile.ui.theme.SurfaceDark
import pl.ultimo.mobile.ui.theme.TextPrimary
import pl.ultimo.mobile.ui.theme.TextSecondary
import pl.ultimo.mobile.ui.theme.UltimoBlue
import pl.ultimo.mobile.ui.theme.UltimoGreen
import pl.ultimo.mobile.ui.theme.UltimoRed
import pl.ultimo.mobile.ui.theme.UltimoYellow

/**
 * Modal dialog presenting a 4-color selector dial for WILD and WILD_DRAW_FOUR card plays.
 *
 * Provides large touch targets for the 4 standard Uno colors with glowing borders
 * and tactile visual feedback.
 */
@Composable
fun WildColorPickerDialog(
    onColorSelected: (CardColor) -> Unit,
    onDismissRequest: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WildDialPulse")
    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BorderGlow"
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(24.dp))
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        listOf(UltimoRed, UltimoBlue, UltimoYellow, UltimoGreen, UltimoRed)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CHOOSE COLOR",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select the active color for the next play",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 2x2 Wheel of Colors
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Row 1: RED and BLUE
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ColorChoiceButton(
                            color = CardColor.RED,
                            displayColor = UltimoRed,
                            label = "RED",
                            glowAlpha = borderGlow,
                            onClick = { onColorSelected(CardColor.RED) }
                        )
                        ColorChoiceButton(
                            color = CardColor.BLUE,
                            displayColor = UltimoBlue,
                            label = "BLUE",
                            glowAlpha = borderGlow,
                            onClick = { onColorSelected(CardColor.BLUE) }
                        )
                    }

                    // Row 2: YELLOW and GREEN
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ColorChoiceButton(
                            color = CardColor.YELLOW,
                            displayColor = UltimoYellow,
                            label = "YELLOW",
                            glowAlpha = borderGlow,
                            onClick = { onColorSelected(CardColor.YELLOW) }
                        )
                        ColorChoiceButton(
                            color = CardColor.GREEN,
                            displayColor = UltimoGreen,
                            label = "GREEN",
                            glowAlpha = borderGlow,
                            onClick = { onColorSelected(CardColor.GREEN) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorChoiceButton(
    color: CardColor,
    displayColor: Color,
    label: String,
    glowAlpha: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(110.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp), spotColor = displayColor)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(displayColor, displayColor.copy(alpha = 0.75f))
                )
            )
            .border(
                width = 3.dp,
                color = Color.White.copy(alpha = glowAlpha),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }
    }
}
