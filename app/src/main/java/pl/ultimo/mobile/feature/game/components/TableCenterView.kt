package pl.ultimo.mobile.feature.game.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor
import pl.ultimo.mobile.core.model.CardValue
import pl.ultimo.mobile.ui.theme.AccentCyan
import pl.ultimo.mobile.ui.theme.SurfaceDark
import pl.ultimo.mobile.ui.theme.TextPrimary
import pl.ultimo.mobile.ui.theme.TextSecondary
import pl.ultimo.mobile.ui.theme.UltimoBlue
import pl.ultimo.mobile.ui.theme.UltimoGreen
import pl.ultimo.mobile.ui.theme.UltimoRed
import pl.ultimo.mobile.ui.theme.UltimoYellow

/**
 * Center table component featuring the Draw Deck and the Discard Pile.
 *
 * Automatically reports Discard Pile boundary coordinates in window root space
 * to power the drag-and-drop collision detection of [DraggableCard].
 */
@Composable
fun TableCenterView(
    topCard: Card?,
    activeColor: CardColor,
    onDrawCard: () -> Unit,
    modifier: Modifier = Modifier,
    drawDeckCount: Int = 30,
    onDropTargetPositioned: (Rect) -> Unit = {}
) {
    val activeGlowColor by animateColorAsState(
        targetValue = when (activeColor) {
            CardColor.RED -> UltimoRed
            CardColor.BLUE -> UltimoBlue
            CardColor.GREEN -> UltimoGreen
            CardColor.YELLOW -> UltimoYellow
            CardColor.WILD -> AccentCyan
        },
        animationSpec = tween(durationMillis = 400),
        label = "ActiveColorGlow"
    )

    Row(
        modifier = modifier
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Draw Deck (Face down cards stacked with count badge)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 120.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onDrawCard)
            ) {
                // Dummy card instance for face down rendering
                CardView(
                    card = Card(CardColor.RED, CardValue.ZERO),
                    isFaceDown = true,
                    elevation = 6.dp
                )

                // Draw Deck count badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(24.dp)
                        .background(SurfaceDark.copy(alpha = 0.9f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$drawDeckCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "DRAW",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 2. Discard Pile (Active top card + drop target detection)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(width = 96.dp, height = 136.dp)
                    .onGloballyPositioned { coordinates ->
                        onDropTargetPositioned(coordinates.boundsInRoot())
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(activeGlowColor.copy(alpha = 0.25f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = activeGlowColor.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (topCard != null) {
                    CardView(
                        card = topCard,
                        isFaceDown = false,
                        isPlayable = false,
                        elevation = 8.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(width = 80.dp, height = 120.dp)
                            .background(SurfaceDark, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "EMPTY",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(activeGlowColor, CircleShape)
                )
                Text(
                    text = "ACTIVE: ${activeColor.name}",
                    style = MaterialTheme.typography.labelMedium,
                    color = activeGlowColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
