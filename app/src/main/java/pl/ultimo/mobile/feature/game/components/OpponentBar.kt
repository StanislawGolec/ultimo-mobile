package pl.ultimo.mobile.feature.game.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
import pl.ultimo.mobile.ui.theme.UltimoBlue
import pl.ultimo.mobile.ui.theme.UltimoRed
import pl.ultimo.mobile.ui.theme.UltimoYellow

private val DisconnectAmber = Color(0xFFFFA000)

/**
 * Responsive opponent carousel scaling up to 15 opponents (16-player capacity).
 *
 * Layout modes:
 * - For <= 3 opponents: Expanded card view (avatar, full handle, card count badge, active turn halo).
 * - For 4 to 15 opponents: Compact pill chip view (avatar icon, truncated handle (max 8 chars),
 *   mini card badge, pulsing active border).
 *
 * Auto-scroll:
 * Uses [rememberLazyListState] to automatically center the currently active turn player
 * in the viewport whenever [activePlayerName] changes.
 *
 * Disconnected status (Stage 13):
 * Grays out chip and displays an amber timer badge "Disconnected (2:00)" counting down.
 */
@Composable
fun OpponentBar(
    opponents: List<OpponentUiModel>,
    activePlayerName: String = "",
    isHost: Boolean = false,
    onKickPlayer: ((String) -> Unit)? = null,
    onTransferHost: ((String) -> Unit)? = null,
    onCatchUltimo: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (opponents.isEmpty()) return

    val listState = rememberLazyListState()
    var selectedOpponentForHost by remember { mutableStateOf<String?>(null) }

    if (isHost && selectedOpponentForHost != null) {
        val target = selectedOpponentForHost!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { selectedOpponentForHost = null },
            title = { Text("Player Actions: $target", color = TextPrimary) },
            text = { Text("Choose a host action for $target.", color = TextSecondary) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        onKickPlayer?.invoke(target)
                        selectedOpponentForHost = null
                    }
                ) {
                    Text("Kick Player ❌", color = UltimoRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        onTransferHost?.invoke(target)
                        selectedOpponentForHost = null
                    }
                ) {
                    Text("Transfer Host 👑", color = UltimoYellow, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Find active turn player index
    val activeIndex = remember(opponents, activePlayerName) {
        val byName = opponents.indexOfFirst { it.name.equals(activePlayerName, ignoreCase = true) }
        if (byName >= 0) byName else opponents.indexOfFirst { it.isCurrentTurn }
    }

    // Auto-scroll logic: automatically center currently active turn player in viewport
    LaunchedEffect(activePlayerName, activeIndex) {
        if (activeIndex in opponents.indices) {
            val viewportWidth = listState.layoutInfo.viewportSize.width
            val visibleItem = listState.layoutInfo.visibleItemsInfo.find { it.index == activeIndex }
            val itemSize = visibleItem?.size ?: if (opponents.size <= 3) 72 else 120
            val offset = if (viewportWidth > 0) ((viewportWidth - itemSize) / 2).coerceAtLeast(0) else 0
            listState.animateScrollToItem(activeIndex, -offset)
        }
    }

    val isCompactMode = opponents.size > 3

    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .testTag("opponent_bar")
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(opponents, key = { it.name }) { opponent ->
            val isActive = opponent.isCurrentTurn || (activePlayerName.isNotBlank() && opponent.name.equals(activePlayerName, ignoreCase = true))
            val effectiveOpponent = if (isActive && !opponent.isCurrentTurn) opponent.copy(isCurrentTurn = true) else opponent

            if (isCompactMode) {
                CompactOpponentChip(
                    opponent = effectiveOpponent,
                    onAvatarClick = { if (isHost) selectedOpponentForHost = opponent.name },
                    onCatchUltimo = onCatchUltimo
                )
            } else {
                ExpandedOpponentCard(
                    opponent = effectiveOpponent,
                    onAvatarClick = { if (isHost) selectedOpponentForHost = opponent.name },
                    onCatchUltimo = onCatchUltimo
                )
            }
        }
    }
}

/**
 * Expanded card view for <= 3 opponents:
 * Avatar circle, full handle, card count badge, active turn halo, and disconnected status.
 */
@Composable
private fun ExpandedOpponentCard(
    opponent: OpponentUiModel,
    onAvatarClick: () -> Unit = {},
    onCatchUltimo: ((String) -> Unit)? = null
) {
    val isUltimo = opponent.cardCount == 1
    val canCatch = opponent.cardCount == 1 && !opponent.hasDeclaredUltimo

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

    val catchPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CatchPulse"
    )

    val haloBorderColor = when {
        opponent.isCurrentTurn -> StatusConnected.copy(alpha = haloPulse)
        canCatch -> UltimoRed.copy(alpha = catchPulse)
        isUltimo -> UltimoYellow.copy(alpha = haloPulse)
        opponent.isDisconnected -> DisconnectAmber.copy(alpha = 0.6f)
        else -> Color.Transparent
    }

    val cardAlpha = if (opponent.isDisconnected) 0.55f else 1.0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .testTag("opponent_item_${opponent.name}")
            .alpha(cardAlpha)
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
                        width = if (opponent.isCurrentTurn || isUltimo || opponent.isDisconnected) 2.5.dp else 1.dp,
                        color = if (opponent.isCurrentTurn || isUltimo || opponent.isDisconnected) haloBorderColor else SurfaceElevatedDark,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .background(SurfaceDark)
                    .clickable(onClick = onAvatarClick),
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

        // Full Handle
        Text(
            text = opponent.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (opponent.isCurrentTurn) StatusConnected else TextSecondary,
            fontWeight = if (opponent.isCurrentTurn) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Disconnected timer badge (Stage 13)
        if (opponent.isDisconnected) {
            val min = opponent.disconnectRemainingSeconds / 60
            val sec = opponent.disconnectRemainingSeconds % 60
            val timeStr = String.format("%d:%02d", min, sec)
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .background(DisconnectAmber.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .border(0.8.dp, DisconnectAmber, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Disconnected ($timeStr)",
                    style = MaterialTheme.typography.labelSmall,
                    color = DisconnectAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp
                )
            }
        } else if (canCatch) {
            // "CATCH ULTIMO!" Chip Badge
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(UltimoRed.copy(alpha = catchPulse))
                    .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .clickable { onCatchUltimo?.invoke(opponent.name) }
                    .testTag("catch_ultimo_${opponent.name}")
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CATCH!",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp
                )
            }
        } else if (isUltimo) {
            // "ULTIMO!" Warning Flag
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

/**
 * Compact pill chip view for 4 to 15 opponents:
 * Circular avatar icon, truncated handle (max 8 chars), mini card count badge,
 * and animated pulsing active border.
 */
@Composable
private fun CompactOpponentChip(
    opponent: OpponentUiModel,
    onAvatarClick: () -> Unit = {},
    onCatchUltimo: ((String) -> Unit)? = null
) {
    val isUltimo = opponent.cardCount == 1
    val canCatch = opponent.cardCount == 1 && !opponent.hasDeclaredUltimo

    val infiniteTransition = rememberInfiniteTransition(label = "CompactHalo")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CompactHaloPulse"
    )

    val catchPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CompactCatchPulse"
    )

    val borderColor = when {
        opponent.isCurrentTurn -> StatusConnected.copy(alpha = haloPulse)
        canCatch -> UltimoRed.copy(alpha = catchPulse)
        isUltimo -> UltimoYellow.copy(alpha = haloPulse)
        opponent.isDisconnected -> DisconnectAmber.copy(alpha = 0.6f)
        else -> SurfaceDark
    }

    val chipAlpha = if (opponent.isDisconnected) 0.55f else 1.0f
    val truncatedHandle = if (opponent.name.length > 8) opponent.name.take(8) else opponent.name

    Row(
        modifier = Modifier
            .testTag("opponent_chip_${opponent.name}")
            .alpha(chipAlpha)
            .clip(RoundedCornerShape(20.dp))
            .background(if (opponent.isDisconnected) SurfaceDark else SurfaceElevatedDark)
            .border(
                width = if (opponent.isCurrentTurn) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Avatar Icon
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(SurfaceDark)
                .border(
                    width = 1.dp,
                    color = if (opponent.isCurrentTurn) StatusConnected else Color.Gray.copy(alpha = 0.4f),
                    shape = CircleShape
                )
                .clickable(onClick = onAvatarClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = opponent.name.take(2).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = if (opponent.isCurrentTurn) AccentCyan else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }

        // 2. Truncated Handle (max 8 chars)
        Text(
            text = truncatedHandle,
            style = MaterialTheme.typography.labelSmall,
            color = if (opponent.isCurrentTurn) StatusConnected else TextSecondary,
            fontWeight = if (opponent.isCurrentTurn) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
            maxLines = 1
        )

        // 3. Mini Card Badge
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (isUltimo) UltimoRed else UltimoBlue)
                .border(0.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${opponent.cardCount}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp
            )
        }

        // 4. "CATCH!" Badge for Compact Mode
        if (canCatch) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(UltimoRed.copy(alpha = catchPulse))
                    .border(0.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                    .clickable { onCatchUltimo?.invoke(opponent.name) }
                    .testTag("catch_ultimo_${opponent.name}")
                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
            ) {
                Text(
                    text = "CATCH!",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 8.sp
                )
            }
        }

        // 5. Disconnected Timer Badge (Stage 13)
        if (opponent.isDisconnected) {
            val min = opponent.disconnectRemainingSeconds / 60
            val sec = opponent.disconnectRemainingSeconds % 60
            val timeStr = String.format("%d:%02d", min, sec)
            Box(
                modifier = Modifier
                    .background(DisconnectAmber.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .border(0.5.dp, DisconnectAmber, RoundedCornerShape(10.dp))
                .padding(horizontal = 5.dp, vertical = 1.5.dp)
            ) {
                Text(
                    text = "Disconnected ($timeStr)",
                    style = MaterialTheme.typography.labelSmall,
                    color = DisconnectAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp
                )
            }
        }
    }
}
