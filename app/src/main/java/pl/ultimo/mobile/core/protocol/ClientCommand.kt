package pl.ultimo.mobile.core.protocol

import androidx.compose.runtime.Immutable
import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor

/**
 * Type-safe, sealed model representing all commands dispatched from the mobile client to the server.
 */
@Immutable
sealed interface ClientCommand {

    val type: CommandType

    data class Join(
        val username: String,
        val roomId: String? = null
    ) : ClientCommand {
        override val type: CommandType = CommandType.JOIN
    }

    data class Play(
        val cards: List<Card>,
        val chosenWildColor: CardColor? = null
    ) : ClientCommand {
        constructor(card: Card, chosenWildColor: CardColor? = null) : this(listOf(card), chosenWildColor)

        val card: Card?
            get() = cards.firstOrNull()

        override val type: CommandType = CommandType.PLAY
    }

    data object Draw : ClientCommand {
        override val type: CommandType = CommandType.DRAW
    }

    data object Pass : ClientCommand {
        override val type: CommandType = CommandType.PASS
    }

    data class Chat(val message: String) : ClientCommand {
        override val type: CommandType = CommandType.CHAT
    }

    data object Ping : ClientCommand {
        override val type: CommandType = CommandType.PING
    }

    data object Start : ClientCommand {
        override val type: CommandType = CommandType.START
    }

    /**
     * Declares 'Ultimo!' when holding 2 cards and playing 1.
     */
    data object Ultimo : ClientCommand {
        override val type: CommandType = CommandType.ULTIMO
    }

    /**
     * Challenges an opponent holding 1 card who failed to declare Ultimo.
     */
    data class CatchUltimo(val targetUsername: String) : ClientCommand {
        override val type: CommandType = CommandType.CATCH_ULTIMO
    }

    /**
     * Challenges the legality of a Wild Draw Four played by the previous player.
     */
    data object ChallengeWildFour : ClientCommand {
        override val type: CommandType = CommandType.CHALLENGE_WILD_FOUR
    }
}
