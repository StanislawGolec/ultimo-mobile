package pl.ultimo.mobile.core.protocol

import androidx.compose.runtime.Immutable
import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor

/**
 * Type-safe, sealed model representing all events emitted from the authoritative server to clients.
 */
@Immutable
sealed interface ServerEvent {

    val type: EventType

    data class Welcome(
        val assignedPlayerId: String,
        val message: String = ""
    ) : ServerEvent {
        override val type: EventType = EventType.WELCOME
    }

    data class RoomStatus(
        val roomId: String,
        val currentPlayers: Int,
        val maxPlayers: Int,
        val playerNames: List<String> = emptyList()
    ) : ServerEvent {
        override val type: EventType = EventType.ROOM_STATUS
    }

    data class HandUpdate(
        val cards: List<Card>
    ) : ServerEvent {
        override val type: EventType = EventType.HAND_UPDATE
    }

    data class TopCard(
        val card: Card,
        val activeColor: CardColor
    ) : ServerEvent {
        override val type: EventType = EventType.TOP_CARD
    }

    data class TurnAnnounced(
        val activePlayerName: String,
        val timeLimitMs: Long = 30000L
    ) : ServerEvent {
        override val type: EventType = EventType.TURN_ANNOUNCED
    }

    data class OpponentUpdate(
        val opponentName: String,
        val cardCount: Int
    ) : ServerEvent {
        override val type: EventType = EventType.OPPONENT_UPDATE
    }

    data class DrawResult(
        val drawnCard: Card,
        val isPlayable: Boolean = false
    ) : ServerEvent {
        override val type: EventType = EventType.DRAW_RESULT
    }

    data class ErrorEvent(
        val errorMessage: String
    ) : ServerEvent {
        override val type: EventType = EventType.ERROR
    }

    data class ChatBroadcast(
        val sender: String,
        val message: String
    ) : ServerEvent {
        override val type: EventType = EventType.CHAT_BROADCAST
    }

    data class GameOver(
        val winnerName: String,
        val scores: String = "",
        val finalScores: Map<String, Int> = emptyMap()
    ) : ServerEvent {
        override val type: EventType = EventType.GAME_OVER
    }

    data class ServerShutdown(
        val drainSeconds: Int = 5
    ) : ServerEvent {
        val drainTimeoutSeconds: Int
            get() = drainSeconds

        override val type: EventType = EventType.SERVER_SHUTDOWN
    }

    data object Pong : ServerEvent {
        override val type: EventType = EventType.PONG
    }

    data class UltimoDeclared(
        val playerName: String
    ) : ServerEvent {
        override val type: EventType = EventType.ULTIMO_DECLARED
    }

    data class UltimoPenalty(
        val penalizedPlayer: String,
        val challenger: String,
        val penaltyCards: Int
    ) : ServerEvent {
        override val type: EventType = EventType.ULTIMO_PENALTY
    }

    data class ChallengeResult(
        val challenger: String,
        val offender: String,
        val isGuilty: Boolean,
        val penalizedPlayer: String,
        val penaltyCards: Int
    ) : ServerEvent {
        override val type: EventType = EventType.CHALLENGE_RESULT
    }

    data class RoundOver(
        val roundWinner: String,
        val pointsEarned: Int,
        val isTournamentWon: Boolean,
        val tournamentScores: Map<String, Int> = emptyMap()
    ) : ServerEvent {
        override val type: EventType = EventType.ROUND_OVER
    }
}
