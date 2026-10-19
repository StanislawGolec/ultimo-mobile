package pl.ultimo.mobile.feature.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import pl.ultimo.mobile.core.audio.SoundEffectsManager
import pl.ultimo.mobile.core.haptic.HapticFeedbackManager
import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor
import pl.ultimo.mobile.core.model.CardValue
import pl.ultimo.mobile.core.network.ConnectionStatus
import pl.ultimo.mobile.feature.game.components.HandCarousel
import pl.ultimo.mobile.feature.game.components.OpponentBar
import pl.ultimo.mobile.feature.game.components.TableCenterView
import pl.ultimo.mobile.feature.game.components.TurnCountdownTimer
import pl.ultimo.mobile.feature.game.dialog.WildColorPickerDialog
import pl.ultimo.mobile.feature.game.model.GameUiEffect
import pl.ultimo.mobile.feature.game.model.GameUiIntent
import pl.ultimo.mobile.feature.game.model.OpponentUiModel
import pl.ultimo.mobile.ui.theme.AccentCyan
import pl.ultimo.mobile.ui.theme.BackgroundDark
import pl.ultimo.mobile.ui.theme.StatusConnected
import pl.ultimo.mobile.ui.theme.StatusDisconnected
import pl.ultimo.mobile.ui.theme.StatusReconnecting
import pl.ultimo.mobile.ui.theme.SurfaceDark
import pl.ultimo.mobile.ui.theme.SurfaceElevatedDark
import pl.ultimo.mobile.ui.theme.TextMuted
import pl.ultimo.mobile.ui.theme.TextPrimary
import pl.ultimo.mobile.ui.theme.TextSecondary
import pl.ultimo.mobile.ui.theme.UltimoBlue
import pl.ultimo.mobile.ui.theme.UltimoRed
import pl.ultimo.mobile.ui.theme.UltimoYellow

/**
 * Stateful GameRoute container wiring [GameViewModel] state flows and single-shot effects
 * to [SoundEffectsManager], [HapticFeedbackManager], and navigation.
 */
@Composable
fun GameRoute(
    roomId: String,
    viewModel: GameViewModel = hiltViewModel(),
    soundManager: SoundEffectsManager? = null,
    hapticManager: HapticFeedbackManager? = null,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Listen to one-off effects (sounds, haptic pulses, navigation)
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is GameUiEffect.PlaySound -> soundManager?.playSound(effect.soundType)
                is GameUiEffect.TriggerHaptic -> hapticManager?.performHaptic(effect.type)
                is GameUiEffect.ShowToast -> { /* non-blocking feedback */ }
                is GameUiEffect.GameOverNav -> { /* game over dialog displayed */ }
            }
        }
    }

    GameTableScreen(
        roomId = uiState.roomId.ifBlank { roomId },
        localPlayerName = uiState.localPlayerName,
        hand = uiState.hand,
        topCard = uiState.topCard,
        activeColor = uiState.activeColor,
        isMyTurn = uiState.isMyTurn,
        activePlayerName = uiState.activePlayerName,
        turnTimeRemainingMs = uiState.turnTimeRemainingMs,
        opponents = uiState.opponents,
        connectionStatus = uiState.connectionStatus,
        isWildPickerVisible = uiState.isWildPickerVisible,
        isUltimoCallable = uiState.isUltimoCallable,
        isHost = uiState.isHost,
        totalPlayerCount = uiState.totalPlayerCount,
        drawDeckCount = uiState.drawDeckCount,
        errorMessage = uiState.errorMessage,
        gameOverWinner = uiState.gameOverWinner,
        onPlayCard = { card ->
            if (card.isWild) {
                viewModel.onIntent(GameUiIntent.CardClicked(card))
            } else {
                viewModel.onIntent(GameUiIntent.CardPlayed(card, null))
            }
        },
        onWildColorSelected = { color ->
            viewModel.onIntent(GameUiIntent.WildColorSelected(color))
        },
        onDrawCard = {
            viewModel.onIntent(GameUiIntent.DrawCard)
        },
        onPassTurn = {
            viewModel.onIntent(GameUiIntent.PassTurn)
        },
        onDeclareUltimo = {
            viewModel.onIntent(GameUiIntent.DeclareUltimo)
        },
        onStartMatch = {
            viewModel.onIntent(GameUiIntent.StartMatch)
        },
        onKickPlayer = { target ->
            viewModel.onIntent(GameUiIntent.KickPlayer(target))
        },
        onTransferHost = { target ->
            viewModel.onIntent(GameUiIntent.TransferHost(target))
        },
        onReconnectNow = {
            viewModel.onIntent(GameUiIntent.ReconnectNow)
        },
        onLeaveRoom = {
            viewModel.onIntent(GameUiIntent.LeaveRoom)
            onNavigateBack()
        }
    )
}

/**
 * Stateless Game Table composable screen.
 *
 * Integrates opponent status indicators, turn countdown gauge, drag-and-drop table arena,
 * bottom fan-shaped hand carousel, reconnection notification banner, host ownership controls,
 * and dialog modals.
 */
@Composable
fun GameTableScreen(
    roomId: String = "room_1",
    localPlayerName: String = "",
    hand: List<Card> = emptyList(),
    topCard: Card? = Card(CardColor.RED, CardValue.SEVEN),
    activeColor: CardColor = CardColor.RED,
    isMyTurn: Boolean = true,
    activePlayerName: String = "Alice",
    turnTimeRemainingMs: Long = 30000L,
    opponents: List<OpponentUiModel> = emptyList(),
    connectionStatus: ConnectionStatus = ConnectionStatus.Connected,
    isWildPickerVisible: Boolean = false,
    isUltimoCallable: Boolean = false,
    isHost: Boolean = false,
    totalPlayerCount: Int = 1,
    drawDeckCount: Int = 32,
    errorMessage: String? = null,
    gameOverWinner: String? = null,
    onPlayCard: (Card) -> Unit = {},
    onWildColorSelected: (CardColor) -> Unit = {},
    onDrawCard: () -> Unit = {},
    onPassTurn: () -> Unit = {},
    onDeclareUltimo: () -> Unit = {},
    onStartMatch: () -> Unit = {},
    onKickPlayer: (String) -> Unit = {},
    onTransferHost: (String) -> Unit = {},
    onReconnectNow: () -> Unit = {},
    onLeaveRoom: () -> Unit = {}
) {
    var dropTargetBounds by remember { mutableStateOf<Rect?>(null) }

    // 1. Wild Color Picker Dialog
    if (isWildPickerVisible) {
        WildColorPickerDialog(
            onColorSelected = onWildColorSelected
        )
    }

    // 2. Game Over Dialog
    if (gameOverWinner != null) {
        GameOverDialog(
            winner = gameOverWinner,
            onReturnToLobby = onLeaveRoom
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Section: Header, Reconnection Banner, Opponent Bar
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Room Title & Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TurnCountdownTimer(
                        timeRemainingMs = turnTimeRemainingMs,
                        size = 36.dp,
                        strokeWidth = 3.dp
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Room: $roomId",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            if (isHost) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(UltimoYellow.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .border(1.dp, UltimoYellow, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "👑 HOST",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = UltimoYellow,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isMyTurn) "YOUR TURN" else "$activePlayerName's Turn",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isMyTurn) StatusConnected else TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = onLeaveRoom,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Exit", color = TextSecondary, fontSize = 11.sp)
                }
            }

            // In-App Reconnection Banner
            AnimatedVisibility(
                visible = connectionStatus is ConnectionStatus.Reconnecting,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                val retrySec = (connectionStatus as? ConnectionStatus.Reconnecting)?.nextRetrySeconds ?: 3
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .background(StatusReconnecting.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .border(1.dp, StatusReconnecting, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Connection Lost: Reconnecting in ${retrySec}s... (You have 2:00 to rejoin seat)",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusReconnecting,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onReconnectNow,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusReconnecting),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "Reconnect Now",
                            color = BackgroundDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Error banner
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
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

            Spacer(modifier = Modifier.height(8.dp))

            // Opponent status indicators with Host Kick & Transfer actions
            OpponentBar(
                opponents = opponents,
                activePlayerName = activePlayerName,
                isHost = isHost,
                onKickPlayer = onKickPlayer,
                onTransferHost = onTransferHost
            )
        }

        // Center Table Area: Discard Pile & Draw Deck
        TableCenterView(
            topCard = topCard,
            activeColor = activeColor,
            drawDeckCount = drawDeckCount,
            onDrawCard = onDrawCard,
            onDropTargetPositioned = { bounds ->
                dropTargetBounds = bounds
            }
        )

        // Bottom Action Controls: Ultimo shout button, Host Start Match button, Pass button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            if (isHost && totalPlayerCount >= 2) {
                Button(
                    onClick = onStartMatch,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = "START MATCH ▶",
                        color = BackgroundDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
            }

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

        // Bottom Section: Local Player Avatar / Status & Hand Carousel
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Local player avatar & crown indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .shadow(if (isMyTurn) 6.dp else 1.dp, CircleShape)
                        .border(
                            width = if (isMyTurn) 2.dp else 1.dp,
                            color = if (isMyTurn) StatusConnected else SurfaceElevatedDark,
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .background(SurfaceDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = localPlayerName.take(2).uppercase().ifEmpty { "ME" },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMyTurn) AccentCyan else TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = localPlayerName.ifEmpty { "You" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                if (isHost) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(UltimoYellow.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .border(1.dp, UltimoYellow, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "👑 HOST",
                            style = MaterialTheme.typography.labelSmall,
                            color = UltimoYellow,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

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

@Composable
private fun GameOverDialog(
    winner: String,
    onReturnToLobby: () -> Unit
) {
    Dialog(onDismissRequest = onReturnToLobby) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .border(2.dp, AccentCyan, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MATCH OVER",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "$winner Emerged Victorious!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AccentCyan,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onReturnToLobby,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UltimoBlue)
                ) {
                    Text("Return to Lobby", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
