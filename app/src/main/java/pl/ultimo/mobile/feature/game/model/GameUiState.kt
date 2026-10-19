package pl.ultimo.mobile.feature.game.model

import androidx.compose.runtime.Immutable
import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor
import pl.ultimo.mobile.core.network.ConnectionStatus

@Immutable
data class OpponentUiModel(
    val name: String,
    val cardCount: Int = 7,
    val isCurrentTurn: Boolean = false,
    val isDisconnected: Boolean = false,
    val disconnectRemainingSeconds: Int = 120,
    val hasDeclaredUltimo: Boolean = false
)

@Immutable
data class GameUiState(
    val roomId: String = "",
    val localPlayerName: String = "",
    val hand: List<Card> = emptyList(),
    val topCard: Card? = null,
    val activeColor: CardColor = CardColor.RED,
    val isMyTurn: Boolean = false,
    val activePlayerName: String = "",
    val turnTimeRemainingMs: Long = 30000L,
    val opponents: List<OpponentUiModel> = emptyList(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected,
    val isWildPickerVisible: Boolean = false,
    val pendingWildCard: Card? = null,
    val isUltimoDeclared: Boolean = false,
    val errorMessage: String? = null,
    val gameOverWinner: String? = null,
    val cardsPlayedCount: Int = 0,
    val roomOwnerUsername: String? = null,
    val pendingSkipDeflection: Boolean = false,
    val pendingDrawPenalty: Int = 0,
    val autoPassNotice: String? = null,
    val isSpectating: Boolean = false,
    val myPlacementRank: Int? = null,
    val isPodiumVisible: Boolean = false,
    val podiumStandings: Map<Int, String> = emptyMap(),
    val drawDeckCount: Int = 108
) {
    val isUltimoCallable: Boolean
        get() = hand.size <= 2 && hand.isNotEmpty() && !isUltimoDeclared

    val isGameOver: Boolean
        get() = gameOverWinner != null

    val isHost: Boolean
        get() = localPlayerName.isNotBlank() && roomOwnerUsername.equals(localPlayerName, ignoreCase = true)

    val totalPlayerCount: Int
        get() = 1 + opponents.size
}
