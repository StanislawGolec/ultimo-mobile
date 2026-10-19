package pl.ultimo.mobile.feature.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import pl.ultimo.mobile.feature.game.components.DefenseHudBanner
import pl.ultimo.mobile.feature.game.components.HandCarousel
import pl.ultimo.mobile.feature.game.components.OpponentBar
import pl.ultimo.mobile.feature.game.components.TableCenterView
import pl.ultimo.mobile.feature.game.components.TurnCountdownTimer
import pl.ultimo.mobile.feature.game.dialog.PodiumDialog
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
import pl.ultimo.mobile.ui.theme.WindowWidthSizeClass
import pl.ultimo.mobile.ui.theme.calculateWindowSizeClass

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
    windowWidthSizeClass: WindowWidthSizeClass = calculateWindowSizeClass().widthSizeClass,
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
        isShoutUltimoVisible = uiState.isShoutUltimoVisible,
        isHost = uiState.isHost,
        totalPlayerCount = uiState.totalPlayerCount,
        drawDeckCount = uiState.drawDeckCount,
        pendingSkipDeflection = uiState.pendingSkipDeflection,
        pendingDrawPenalty = uiState.pendingDrawPenalty,
        autoPassNotice = uiState.autoPassNotice,
        isSpectating = uiState.isSpectating,
        myPlacementRank = uiState.myPlacementRank,
        isPodiumVisible = uiState.isPodiumVisible,
        podiumStandings = uiState.podiumStandings,
        errorMessage = uiState.errorMessage,
        gameOverWinner = uiState.gameOverWinner,
        windowWidthSizeClass = windowWidthSizeClass,
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
        onCatchUltimo = { target ->
            viewModel.onIntent(GameUiIntent.CatchUltimo(target))
        },
        onDismissAutoPass = {
            viewModel.onIntent(GameUiIntent.DismissAutoPass)
        },
        onDismissPodium = {
            viewModel.onIntent(GameUiIntent.DismissPodium)
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
 * Integrates opponent status indicators, turn countdown gauge, tactical defense HUD banner,
 * drag-and-drop table arena, bottom fan-shaped hand carousel (or spectator bar),
 * reconnection notification banner, host ownership controls, dialog modals,
 * and Stage 14 WindowWidthSizeClass adaptive layouts (Compact, Medium, Expanded).
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
    isShoutUltimoVisible: Boolean = false,
    isHost: Boolean = false,
    totalPlayerCount: Int = 1,
    drawDeckCount: Int = 32,
    pendingSkipDeflection: Boolean = false,
    pendingDrawPenalty: Int = 0,
    autoPassNotice: String? = null,
    isSpectating: Boolean = false,
    myPlacementRank: Int? = null,
    isPodiumVisible: Boolean = false,
    podiumStandings: Map<Int, String> = emptyMap(),
    errorMessage: String? = null,
    gameOverWinner: String? = null,
    windowWidthSizeClass: WindowWidthSizeClass = calculateWindowSizeClass().widthSizeClass,
    onPlayCard: (Card) -> Unit = {},
    onWildColorSelected: (CardColor) -> Unit = {},
    onDrawCard: () -> Unit = {},
    onPassTurn: () -> Unit = {},
    onDeclareUltimo: () -> Unit = {},
    onCatchUltimo: (String) -> Unit = {},
    onDismissAutoPass: () -> Unit = {},
    onDismissPodium: () -> Unit = {},
    onStartMatch: () -> Unit = {},
    onKickPlayer: (String) -> Unit = {},
    onTransferHost: (String) -> Unit = {},
    onReconnectNow: () -> Unit = {},
    onCopyRoomCode: (String) -> Unit = {},
    onLeaveRoom: () -> Unit = {}
) {
    var dropTargetBounds by remember { mutableStateOf<Rect?>(null) }
    var isRoomCopied by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    // 1. Wild Color Picker Dialog
    if (isWildPickerVisible) {
        WildColorPickerDialog(
            onColorSelected = onWildColorSelected
        )
    }

    // 2. Stepped Podium Standings Dialog (Stage 12)
    if (isPodiumVisible) {
        PodiumDialog(
            standings = podiumStandings,
            onReturnToLobby = onLeaveRoom,
            onDismissRequest = onDismissPodium
        )
    } else if (gameOverWinner != null) {
        // Fallback single-winner Game Over Dialog
        GameOverDialog(
            winner = gameOverWinner,
            onReturnToLobby = onLeaveRoom
        )
    }

    val topHeaderAndHudSection: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Room Title, Copy Action & Header Bar
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
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("room_code_header")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceElevatedDark)
                                    .border(1.dp, AccentCyan.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(roomId))
                                        isRoomCopied = true
                                        onCopyRoomCode(roomId)
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("btn_copy_room_code")
                            ) {
                                Text(
                                    text = if (isRoomCopied) "✓ Copied!" else "📋 Copy",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
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

            // Opponent status indicators with Host Kick, Transfer & Catch Ultimo actions
            OpponentBar(
                opponents = opponents,
                activePlayerName = activePlayerName,
                isHost = isHost,
                onKickPlayer = onKickPlayer,
                onTransferHost = onTransferHost,
                onCatchUltimo = onCatchUltimo
            )

            // Stage 11: Action Defending HUD Banner
            DefenseHudBanner(
                pendingSkipDeflection = pendingSkipDeflection,
                pendingDrawPenalty = pendingDrawPenalty,
                autoPassNotice = autoPassNotice,
                onTakePenalty = onDrawCard,
                onDismissAutoPass = onDismissAutoPass
            )
        }
    }

    val centerTableSection: @Composable () -> Unit = {
        TableCenterView(
            topCard = topCard,
            activeColor = activeColor,
            drawDeckCount = drawDeckCount,
            pendingDrawPenalty = pendingDrawPenalty,
            onDrawCard = onDrawCard,
            onDropTargetPositioned = { bounds ->
                dropTargetBounds = bounds
            }
        )
    }

    val bottomControlsAndHandSection: @Composable () -> Unit = {
        val canShoutUltimo = isShoutUltimoVisible || isUltimoCallable || (hand.size in 1..2 && !isSpectating)
        val starburstPulse = rememberInfiniteTransition(label = "StarburstPulse")
        val starburstScale by starburstPulse.animateFloat(
            initialValue = 1.0f,
            targetValue = if (canShoutUltimo) 1.08f else 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "StarburstScale"
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onDeclareUltimo,
                    enabled = canShoutUltimo,
                    modifier = Modifier
                        .scale(if (canShoutUltimo) starburstScale else 1f)
                        .testTag("btn_shout_ultimo"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        disabledContainerColor = SurfaceDark
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (canShoutUltimo) 8.dp else 0.dp)
                ) {
                    Text(
                        text = if (canShoutUltimo) "★ SHOUT ULTIMO! ★" else "ULTIMO!",
                        color = if (canShoutUltimo) BackgroundDark else TextMuted,
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
                    enabled = isMyTurn && !isSpectating,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AccentCyan,
                        disabledContentColor = TextMuted
                    )
                ) {
                    Text("Pass Turn", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

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

            if (isSpectating) {
                // Stage 12: Spectator Camera Mode Banner for Finished Players
                val rankLabel = when (myPlacementRank) {
                    1 -> "1ST"
                    2 -> "2ND"
                    3 -> "3RD"
                    null -> "FINISHED"
                    else -> "${myPlacementRank}TH"
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1E293B),
                                    Color(0xFF0F172A),
                                    Color(0xFF1E293B)
                                )
                            )
                        )
                        .border(2.dp, Color(0xFFFFD700).copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                        .testTag("spectator_banner"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🏆 $rankLabel PLACE! You finished!",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Spectating remaining match in real time...",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentCyan,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                HandCarousel(
                    hand = hand,
                    topCard = topCard,
                    activeColor = activeColor,
                    isMyTurn = isMyTurn,
                    pendingSkipDeflection = pendingSkipDeflection,
                    pendingDrawPenalty = pendingDrawPenalty,
                    dropTargetBounds = dropTargetBounds,
                    onPlayCard = onPlayCard
                )
            }
        }
    }

    // Stage 14: Responsive WindowSizeClass Orchestration
    when (windowWidthSizeClass) {
        WindowWidthSizeClass.Expanded -> {
            // Dual-column layout for Tablets (>840dp), Desktop DeX, and Ultrawide displays
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(vertical = 12.dp, horizontal = 20.dp)
                    .testTag("layout_expanded"),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("expanded_left_column"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    topHeaderAndHudSection()
                    centerTableSection()
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("expanded_right_column"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    bottomControlsAndHandSection()
                }
            }
        }
        WindowWidthSizeClass.Medium -> {
            // Foldable / Small Tablet layout (600-840dp) with wider spacing
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(vertical = 12.dp, horizontal = 24.dp)
                    .testTag("layout_medium"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                topHeaderAndHudSection()
                centerTableSection()
                bottomControlsAndHandSection()
            }
        }
        WindowWidthSizeClass.Compact -> {
            // Standard Phone layout (<600dp)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(vertical = 8.dp, horizontal = 12.dp)
                    .testTag("layout_compact"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                topHeaderAndHudSection()
                centerTableSection()
                bottomControlsAndHandSection()
            }
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
