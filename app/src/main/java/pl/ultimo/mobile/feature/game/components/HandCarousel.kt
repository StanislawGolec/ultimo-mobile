package pl.ultimo.mobile.feature.game.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor
import pl.ultimo.mobile.core.model.CardValue
import pl.ultimo.mobile.ui.theme.AccentCyan
import pl.ultimo.mobile.ui.theme.TextMuted
import kotlin.math.min

/**
 * Dynamic fan-shaped hand carousel displaying the player's active cards.
 *
 * Mathematically calculates angular rotation (\theta_i) and vertical parabolic arch offsets (y_i)
 * based on hand size, automatically compressing horizontal card spacing as hand size scales
 * up to 15+ cards. Provides horizontal scrolling when hand size exceeds container width.
 */
@Composable
fun HandCarousel(
    hand: List<Card>,
    topCard: Card?,
    activeColor: CardColor,
    isMyTurn: Boolean,
    modifier: Modifier = Modifier,
    pendingSkipDeflection: Boolean = false,
    pendingDrawPenalty: Int = 0,
    dropTargetBounds: Rect? = null,
    onPlayCard: (Card) -> Unit = {}
) {
    if (hand.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No cards remaining",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
        return
    }

    var selectedCardId by remember { mutableStateOf<String?>(null) }
    val handCount = hand.size
    val isDefenseActive = pendingSkipDeflection || pendingDrawPenalty > 0

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 8.dp)
    ) {
        val availableWidth = maxWidth
        val handSize = hand.size
        val cardWidth = (maxWidth / (handSize.coerceAtLeast(7) + 2)).coerceIn(60.dp, 120.dp)
        val cardHeight = cardWidth * 1.5f
        val centerIndex = (handCount - 1) / 2.0f

        // Calculate responsive horizontal spacing between cards
        val spacing: Dp = if (handCount <= 1) {
            0.dp
        } else {
            val naturalSpacing = (availableWidth - cardWidth - 16.dp) / (handCount - 1)
            naturalSpacing.coerceIn(24.dp, 68.dp)
        }

        val totalWidth = if (handCount <= 1) cardWidth else (spacing * (handCount - 1) + cardWidth)
        val shouldScroll = totalWidth > availableWidth
        val startOffset = if (shouldScroll) 8.dp else (availableWidth - totalWidth) / 2f
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .then(
                    if (shouldScroll) {
                        Modifier.horizontalScroll(scrollState)
                    } else Modifier
                )
                .width(if (shouldScroll) totalWidth + 32.dp else availableWidth)
                .height(180.dp)
        ) {
            hand.forEachIndexed { index, card ->
                val cardToken = "${card.toWireToken()}_$index"
                val isSelected = selectedCardId == cardToken
                val isPlayable = isMyTurn && card.canPlayOn(
                    topCard = topCard,
                    activeColor = activeColor,
                    pendingSkipDeflection = pendingSkipDeflection,
                    pendingDrawPenalty = pendingDrawPenalty
                )
                val isDimmed = isDefenseActive && !isPlayable
                val isSkipDeflectionCard = pendingSkipDeflection && card.value == CardValue.SKIP

                // Fan geometry calculation
                val relativeIndex = index - centerIndex

                // Angular rotation: cards fan out from center
                val angleStep = when {
                    handCount <= 5 -> 4.5f
                    handCount <= 9 -> 3.2f
                    else -> (32f / handCount).coerceAtMost(2.5f)
                }
                val rotationAngle = (relativeIndex * angleStep).coerceIn(-26f, 26f)

                // Vertical parabolic arch: edge cards droop down naturally
                val arcStep = when {
                    handCount <= 5 -> 2.2f
                    handCount <= 9 -> 1.5f
                    else -> min(1.0f, 14f / handCount)
                }
                val archOffsetDp = ((relativeIndex * relativeIndex) * arcStep).dp

                val cardX = startOffset + spacing * index

                DraggableCard(
                    card = card,
                    isPlayable = isPlayable,
                    initialRotation = rotationAngle,
                    baseVerticalOffset = archOffsetDp + 24.dp,
                    isSelected = isSelected && !isDimmed,
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    dropTargetBounds = dropTargetBounds,
                    modifier = Modifier
                        .offset(x = cardX)
                        .graphicsLayer {
                            alpha = if (isDimmed) 0.4f else 1.0f
                        }
                        .then(
                            if (isSkipDeflectionCard) {
                                Modifier.border(2.5.dp, AccentCyan, RoundedCornerShape(10.dp))
                            } else Modifier
                        )
                        .testTag("hand_card_${card.toWireToken()}"),
                    onCardClicked = { clickedCard ->
                        if (!isDimmed) {
                            if (isSelected) {
                                // Second tap on selected card plays it if legal
                                if (isPlayable) {
                                    onPlayCard(clickedCard)
                                    selectedCardId = null
                                }
                            } else {
                                selectedCardId = cardToken
                            }
                        }
                    },
                    onCardPlayed = { playedCard ->
                        if (!isDimmed) {
                            onPlayCard(playedCard)
                            selectedCardId = null
                        }
                    }
                )
            }
        }
    }
}
