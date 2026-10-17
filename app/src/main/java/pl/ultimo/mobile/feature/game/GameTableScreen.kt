package pl.ultimo.mobile.feature.game

import androidx.compose.foundation.background
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor
import pl.ultimo.mobile.core.model.CardValue
import pl.ultimo.mobile.ui.theme.BackgroundDark
import pl.ultimo.mobile.ui.theme.CardShape
import pl.ultimo.mobile.ui.theme.StatusConnected
import pl.ultimo.mobile.ui.theme.SurfaceDark
import pl.ultimo.mobile.ui.theme.TextPrimary
import pl.ultimo.mobile.ui.theme.TextSecondary
import pl.ultimo.mobile.ui.theme.UltimoBlue
import pl.ultimo.mobile.ui.theme.UltimoRed

@Composable
fun GameTableScreen(
    roomId: String = "room_1",
    hand: List<Card> = emptyList(),
    topCard: Card? = Card(CardColor.RED, CardValue.SEVEN),
    activeColor: CardColor = CardColor.RED,
    isMyTurn: Boolean = true,
    activePlayerName: String = "Alice",
    onPlayCard: (Card) -> Unit = {},
    onDrawCard: () -> Unit = {},
    onLeaveRoom: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header: Room ID & Leave button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Room: $roomId",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = if (isMyTurn) "Your Turn!" else "Waiting for $activePlayerName...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isMyTurn) StatusConnected else TextSecondary
                )
            }

            Button(
                onClick = onLeaveRoom,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Exit", color = TextSecondary)
            }
        }

        // Center Table Discard Pile & Draw Deck
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Draw Deck Pile
            Surface(
                onClick = onDrawCard,
                modifier = Modifier
                    .size(width = 90.dp, height = 130.dp),
                shape = CardShape,
                color = SurfaceDark,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("DRAW", style = MaterialTheme.typography.labelLarge, color = UltimoBlue)
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Discard Top Card Display
            Surface(
                modifier = Modifier
                    .size(width = 90.dp, height = 130.dp),
                shape = CardShape,
                color = when (activeColor) {
                    CardColor.RED -> UltimoRed
                    CardColor.BLUE -> UltimoBlue
                    else -> UltimoRed
                },
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = topCard?.toWireToken() ?: "WAIT",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
        }

        // Bottom Hand Carousel Placeholder
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cards in Hand (${hand.size})",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                if (hand.isEmpty()) {
                    Text("No cards in hand. Draw to begin.", color = TextSecondary)
                } else {
                    hand.take(5).forEach { card ->
                        Surface(
                            onClick = { onPlayCard(card) },
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(width = 60.dp, height = 90.dp),
                            shape = CardShape,
                            color = UltimoBlue
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(card.value.name.take(3), color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
