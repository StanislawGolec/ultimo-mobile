package pl.ultimo.mobile.feature.game.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.ultimo.mobile.feature.game.model.OpponentUiModel
import pl.ultimo.mobile.ui.theme.AccentCyan
import pl.ultimo.mobile.ui.theme.StatusConnected
import pl.ultimo.mobile.ui.theme.SurfaceDark
import pl.ultimo.mobile.ui.theme.SurfaceElevatedDark
import pl.ultimo.mobile.ui.theme.TextPrimary
import pl.ultimo.mobile.ui.theme.TextSecondary
import pl.ultimo.mobile.ui.theme.UltimoRed
import pl.ultimo.mobile.ui.theme.UltimoYellow

/**
 * Top opponent bar displaying connected opponents with card count badges and active turn halos.
 */
@Composable
fun OpponentBar(
    opponents: List<OpponentUiModel>,
    modifier: Modifier = Modifier
) {
    if (opponents.isEmpty()) return

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(opponents, key = { it.name }) { opponent ->
            OpponentAvatarItem(opponent = opponent)
        }
    }
}

@Composable
private fun OpponentAvatarItem(
    opponent: OpponentUiModel
) {
    val isUltimo = opponent.cardCount == 1

    val infiniteTransition = rememberInfiniteTransition(label = "TurnHalo")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloPulse"
    )

    val haloBorderColor = when {
        opponent.isCurrentTurn -> StatusConnected.copy(alpha = haloPulse)
        isUltimo -> UltimoYellow.copy(alpha = haloPulse)
        else -> Color.Transparent
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Avatar Circle
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(elevation = if (opponent.isCurrentTurn) 8.dp else 2.dp, shape = CircleShape)
                    .border(
                        width = if (opponent.isCurrentTurn || isUltimo) 2.5.dp else 1.dp,
                        color = if (opponent.isCurrentTurn || isUltimo) haloBorderColor else SurfaceElevatedDark,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .background(SurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = opponent.name.take(2).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (opponent.isCurrentTurn) AccentCyan else TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Card Count Badge (bottom right overlap)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isUltimo) UltimoRed else SurfaceElevatedDark)
                    .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${opponent.cardCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Opponent Name
        Text(
            text = opponent.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (opponent.isCurrentTurn) StatusConnected else TextSecondary,
            fontWeight = if (opponent.isCurrentTurn) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // "ULTIMO!" Warning Flag
        if (isUltimo) {
            Text(
                text = "ULTIMO!",
                style = MaterialTheme.typography.labelSmall,
                color = UltimoYellow,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp
            )
        }
    }
}
