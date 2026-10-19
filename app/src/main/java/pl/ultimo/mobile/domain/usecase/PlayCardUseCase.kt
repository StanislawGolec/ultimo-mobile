package pl.ultimo.mobile.domain.usecase

import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor
import pl.ultimo.mobile.core.network.WebSocketEngine
import pl.ultimo.mobile.core.protocol.ClientCommand
import javax.inject.Inject

class PlayCardUseCase @Inject constructor(
    private val webSocketEngine: WebSocketEngine
) {
    operator fun invoke(
        card: Card,
        chosenWildColor: CardColor? = null,
        topCard: Card?,
        activeColor: CardColor?,
        pendingSkipDeflection: Boolean = false,
        pendingDrawPenalty: Int = 0
    ): Result<Unit> {
        // Validate wild card color selection
        if (card.isWild && chosenWildColor == null) {
            return Result.failure(IllegalArgumentException("Wild card requires a declared color (RED, BLUE, GREEN, or YELLOW)"))
        }

        // Validate play legality against table top card & tactical defense status
        if (!card.canPlayOn(topCard, activeColor, pendingSkipDeflection, pendingDrawPenalty)) {
            val reason = when {
                pendingSkipDeflection -> "Only SKIP cards can deflect a pending skip attack"
                pendingDrawPenalty > 0 -> "Only matching draw cards can defend against +$pendingDrawPenalty penalty stack"
                else -> "Card $card cannot be legally played on top card $topCard (active color: $activeColor)"
            }
            return Result.failure(IllegalStateException(reason))
        }

        val command = ClientCommand.Play(card, chosenWildColor)
        val success = webSocketEngine.sendCommand(command)

        return if (success) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("Failed to transmit PLAY command across active socket"))
        }
    }
}
