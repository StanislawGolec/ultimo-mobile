package pl.ultimo.mobile.feature.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor
import pl.ultimo.mobile.core.model.CardValue
import pl.ultimo.mobile.feature.game.components.HandCarousel
import pl.ultimo.mobile.feature.game.components.TableCenterView
import pl.ultimo.mobile.ui.theme.AccentCyan
import pl.ultimo.mobile.ui.theme.BackgroundDark
import pl.ultimo.mobile.ui.theme.StatusConnected
import pl.ultimo.mobile.ui.theme.StatusDisconnected
import pl.ultimo.mobile.ui.theme.SurfaceDark
import pl.ultimo.mobile.ui.theme.SurfaceElevatedDark
import pl.ultimo.mobile.ui.theme.TextMuted
import pl.ultimo.mobile.ui.theme.TextPrimary
import pl.ultimo.mobile.ui.theme.TextSecondary
import pl.ultimo.mobile.ui.theme.UltimoRed
import pl.ultimo.mobile.ui.theme.UltimoYellow

/**
 * Main Game Table composable screen.
 *
 * Integrates custom vector card rendering, real-time drag physics with spring-back recovery,
 * dynamic fan-shaped hand carousel, and center discard pile drop target recognition.
 */
@Composable
fun GameTableScreen(
    roomId: String = "room_1",
    hand: List<Card> = emptyList(),
    topCard: Card? = Card(CardColor.RED, CardValue.SEVEN),
    activeColor: CardColor = CardColor.RED,
    isMyTurn: Boolean = true,
    activePlayerName: String = "Alice",
    isUltimoCallable: Boolean = false,
    drawDeckCount: Int = 32,
    errorMessage: String? = null,
    onPlayCard: (Card) -> Unit = {},
    onDrawCard: () -> Unit = {},
    onPassTurn: () -> Unit = {},
    onDeclareUltimo: () -> Unit = {},
    onLeaveRoom: () -> Unit = {}
) {
    var dropTargetBounds by remember { mutableStateOf<Rect?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Top Header: Room ID, active turn status indicator, and leave button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (isMyTurn) StatusConnected else StatusDisconnected, CircleShape)
                    )
                    Text(
                        text = "Room: $roomId",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isMyTurn) "Your Turn! Drag or tap a card to play" else "Waiting for $activePlayerName...",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isMyTurn) StatusConnected else TextSecondary
                )
            }

            Button(
                onClick = onLeaveRoom,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Exit", color = TextSecondary, fontSize = 12.sp)
            }
        }

        // Error message banner if illegal move or network rejection occurred
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(UltimoRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .border(1.dp, UltimoRed, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 2. Center Table Arena: Discard Pile & Draw Deck
        TableCenterView(
            topCard = topCard,
            activeColor = activeColor,
            drawDeckCount = drawDeckCount,
            onDrawCard = onDrawCard,
            onDropTargetPositioned = { bounds ->
                dropTargetBounds = bounds
            }
        )

        // 3. Action Controls: Ultimo shout button, Pass button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Ultimo!" Shout Button
            Button(
                onClick = onDeclareUltimo,
                enabled = isUltimoCallable,
                colors = ButtonDefaults.buttonColors(
                    containerColor = UltimoYellow,
                    disabledContainerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "ULTIMO!",
                    color = if (isUltimoCallable) BackgroundDark else TextMuted,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }

            // Pass Turn Button (usable after drawing)
            OutlinedButton(
                onClick = onPassTurn,
                enabled = isMyTurn,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AccentCyan,
                    disabledContentColor = TextMuted
                )
            ) {
                Text("Pass Turn", fontSize = 13.sp)
            }
        }

        // 4. Bottom Player Hand Carousel
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HandCarousel(
                hand = hand,
                topCard = topCard,
                activeColor = activeColor,
                isMyTurn = isMyTurn,
                dropTargetBounds = dropTargetBounds,
                onPlayCard = onPlayCard
            )
        }
    }
}
