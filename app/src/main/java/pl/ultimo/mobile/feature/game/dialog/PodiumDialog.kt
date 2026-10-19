package pl.ultimo.mobile.feature.game.dialog

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pl.ultimo.mobile.ui.theme.AccentCyan
import pl.ultimo.mobile.ui.theme.BackgroundDark
import pl.ultimo.mobile.ui.theme.SurfaceContainerDark
import pl.ultimo.mobile.ui.theme.SurfaceDark
import pl.ultimo.mobile.ui.theme.TextPrimary
import pl.ultimo.mobile.ui.theme.TextSecondary
import pl.ultimo.mobile.ui.theme.UltimoRed
import pl.ultimo.mobile.ui.theme.UltimoYellow

private val GoldGradient = listOf(Color(0xFFFFDF00), Color(0xFFD4AF37), Color(0xFF996515))
private val SilverGradient = listOf(Color(0xFFE0E0E0), Color(0xFFB0BEC5), Color(0xFF78909C))
private val BronzeGradient = listOf(Color(0xFFCD7F32), Color(0xFFA0522D), Color(0xFF6E260E))
private val SubmergedGradient = listOf(Color(0xFF374151), Color(0xFF1F2937), Color(0xFF111827))

/**
 * Animated 3D stepped podium standings dialog triggered upon match completion.
 *
 * Visually showcases:
 * - 1st Place: Gold pedestal (tallest) with crown/trophy accent.
 * - 2nd Place: Silver pedestal (medium).
 * - 3rd Place: Bronze pedestal (compact).
 * - Last Place (>= 4 players): Submerged cracked pedestal with skull icon.
 */
@Composable
fun PodiumDialog(
    standings: Map<Int, String>,
    onReturnToLobby: () -> Unit,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {}
) {
    var isRisen by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isRisen = true
    }

    val p1Height by animateDpAsState(
        targetValue = if (isRisen) 130.dp else 20.dp,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "P1Height"
    )
    val p2Height by animateDpAsState(
        targetValue = if (isRisen) 90.dp else 20.dp,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "P2Height"
    )
    val p3Height by animateDpAsState(
        targetValue = if (isRisen) 60.dp else 20.dp,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "P3Height"
    )

    val lastRank = standings.keys.maxOrNull() ?: 4
    val hasSubmerged = standings.size >= 4 || lastRank >= 4
    val lastName = standings[lastRank] ?: ""

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = modifier
                .padding(20.dp)
                .fillMaxWidth(0.95f)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .border(2.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .testTag("podium_dialog"),
            colors = CardDefaults.cardColors(containerColor = BackgroundDark),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Match Standings Trophy",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "MATCH STANDINGS",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Last-Man-Standing Battle Results",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // 3-Column Olympic Podium: 2nd (Silver), 1st (Gold), 3rd (Bronze)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // 2nd Place: Silver
                    PodiumPedestal(
                        rank = 2,
                        label = "2nd",
                        playerName = standings[2] ?: "-",
                        height = p2Height,
                        gradient = SilverGradient,
                        borderColor = Color(0xFFCFD8DC),
                        modifier = Modifier.testTag("podium_pedestal_2")
                    )

                    // 1st Place: Gold (Center & Tallest)
                    PodiumPedestal(
                        rank = 1,
                        label = "1st",
                        playerName = standings[1] ?: "-",
                        height = p1Height,
                        gradient = GoldGradient,
                        borderColor = Color(0xFFFFD700),
                        isWinner = true,
                        modifier = Modifier.testTag("podium_pedestal_1")
                    )

                    // 3rd Place: Bronze
                    PodiumPedestal(
                        rank = 3,
                        label = "3rd",
                        playerName = standings[3] ?: "-",
                        height = p3Height,
                        gradient = BronzeGradient,
                        borderColor = Color(0xFFCD7F32),
                        modifier = Modifier.testTag("podium_pedestal_3")
                    )
                }

                // Submerged Pedestal for Last Place (if >= 4 players or last rank is known)
                if (hasSubmerged && lastName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(SubmergedGradient))
                            .border(1.5.dp, UltimoRed.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("podium_pedestal_last"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "💀",
                                fontSize = 18.sp
                            )
                            Column {
                                Text(
                                    text = "SUBMERGED (#$lastRank): $lastName",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFFCA5A5),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Last Man Sunk into the Abyss",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Return to Lobby Action Button
                Button(
                    onClick = onReturnToLobby,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(48.dp)
                        .testTag("btn_return_to_lobby"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentCyan,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Return to Lobby",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumPedestal(
    rank: Int,
    label: String,
    playerName: String,
    height: Dp,
    gradient: List<Color>,
    borderColor: Color,
    modifier: Modifier = Modifier,
    isWinner: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(88.dp)
    ) {
        // Crown or Rank Badge atop Pedestal
        if (isWinner) {
            Text(
                text = "👑",
                fontSize = 22.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        // Player Name
        Text(
            text = playerName,
            style = MaterialTheme.typography.labelMedium,
            color = if (isWinner) Color(0xFFFFD700) else TextPrimary,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Stepped Pedestal Block
        Box(
            modifier = Modifier
                .width(76.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(Brush.verticalGradient(gradient))
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
