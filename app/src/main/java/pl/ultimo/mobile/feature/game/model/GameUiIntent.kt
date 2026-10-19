package pl.ultimo.mobile.feature.game.model

import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor

/**
 * User interactions and events originating from the GameTableScreen UI.
 */
sealed interface GameUiIntent {
    data class CardClicked(val card: Card) : GameUiIntent
    data class CardPlayed(val card: Card, val chosenWildColor: CardColor? = null) : GameUiIntent
    data class WildColorSelected(val color: CardColor) : GameUiIntent
    data object WildPickerDismissed : GameUiIntent
    data object DrawCard : GameUiIntent
    data object PassTurn : GameUiIntent
    data object DeclareUltimo : GameUiIntent
    data class SendChat(val message: String) : GameUiIntent
    data object DismissError : GameUiIntent
    data object LeaveRoom : GameUiIntent
    data object ReconnectNow : GameUiIntent

    // Host Ownership Controls
    data object StartMatch : GameUiIntent
    data class KickPlayer(val targetUsername: String) : GameUiIntent
    data class TransferHost(val targetUsername: String) : GameUiIntent
}
