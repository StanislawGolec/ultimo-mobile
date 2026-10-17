package pl.ultimo.mobile.feature.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pl.ultimo.mobile.core.database.dao.CardStatDao
import pl.ultimo.mobile.core.datastore.UserPreferencesRepository
import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor
import pl.ultimo.mobile.core.network.WebSocketEngine
import pl.ultimo.mobile.core.protocol.ServerEvent
import pl.ultimo.mobile.domain.usecase.DeclareUltimoUseCase
import pl.ultimo.mobile.domain.usecase.DrawCardUseCase
import pl.ultimo.mobile.domain.usecase.PassTurnUseCase
import pl.ultimo.mobile.domain.usecase.PlayCardUseCase
import pl.ultimo.mobile.domain.usecase.RecordMatchOutcomeUseCase
import pl.ultimo.mobile.feature.game.model.GameUiEffect
import pl.ultimo.mobile.feature.game.model.GameUiIntent
import pl.ultimo.mobile.feature.game.model.GameUiState
import pl.ultimo.mobile.feature.game.model.HapticType
import pl.ultimo.mobile.feature.game.model.OpponentUiModel
import pl.ultimo.mobile.feature.game.model.SoundType
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val webSocketEngine: WebSocketEngine,
    private val playCardUseCase: PlayCardUseCase,
    private val drawCardUseCase: DrawCardUseCase,
    private val passTurnUseCase: PassTurnUseCase,
    private val declareUltimoUseCase: DeclareUltimoUseCase,
    private val recordMatchOutcomeUseCase: RecordMatchOutcomeUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val cardStatDao: CardStatDao
) : ViewModel() {

    private val roomId: String = savedStateHandle["roomId"] ?: "default_room"

    private val _uiState = MutableStateFlow(GameUiState(roomId = roomId))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<GameUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<GameUiEffect> = _uiEffect.receiveAsFlow()

    private var turnTimerJob: Job? = null
    private var matchStartTimeMs: Long = System.currentTimeMillis()

    init {
        // Resolve saved local player handle
        viewModelScope.launch {
            val prefs = userPreferencesRepository.userPreferences.firstOrNull()
            val handle = prefs?.savedPlayerHandle ?: "Player_1"
            _uiState.update { it.copy(localPlayerName = handle) }
        }

        // Stream connection status
        viewModelScope.launch {
            webSocketEngine.connectionStatus.collect { status ->
                _uiState.update { it.copy(connectionStatus = status) }
            }
        }

        // Stream authoritative server events
        viewModelScope.launch {
            webSocketEngine.events.collect { event ->
                handleServerEvent(event)
            }
        }

        // Stream connection or protocol errors
        viewModelScope.launch {
            webSocketEngine.errors.collect { errorMsg ->
                _uiState.update { it.copy(errorMessage = errorMsg) }
            }
        }
    }

    private fun handleServerEvent(event: ServerEvent) {
        when (event) {
            is ServerEvent.Welcome -> {
                matchStartTimeMs = System.currentTimeMillis()
            }
            is ServerEvent.RoomStatus -> {
                val opponentList = event.playerNames
                    .filter { !it.equals(_uiState.value.localPlayerName, ignoreCase = true) }
                    .map { name -> OpponentUiModel(name = name) }
                _uiState.update { it.copy(opponents = opponentList) }
            }
            is ServerEvent.HandUpdate -> {
                _uiState.update { state ->
                    val isResetUltimo = event.cards.size > 1
                    state.copy(
                        hand = event.cards,
                        isUltimoDeclared = if (isResetUltimo) false else state.isUltimoDeclared
                    )
                }
            }
            is ServerEvent.TopCard -> {
                val previousTop = _uiState.value.topCard
                _uiState.update {
                    it.copy(
                        topCard = event.card,
                        activeColor = event.activeColor
                    )
                }
                if (previousTop != null && previousTop != event.card) {
                    emitEffect(GameUiEffect.PlaySound(SoundType.CARD_PLAY))
                }
            }
            is ServerEvent.TurnAnnounced -> {
                val isMyTurn = event.activePlayerName.equals(_uiState.value.localPlayerName, ignoreCase = true)
                _uiState.update { state ->
                    val updatedOpponents = state.opponents.map {
                        it.copy(isCurrentTurn = it.name.equals(event.activePlayerName, ignoreCase = true))
                    }
                    state.copy(
                        isMyTurn = isMyTurn,
                        activePlayerName = event.activePlayerName,
                        turnTimeRemainingMs = event.timeLimitMs,
                        opponents = updatedOpponents
                    )
                }

                startTurnCountdown(event.timeLimitMs)

                if (isMyTurn) {
                    emitEffect(GameUiEffect.PlaySound(SoundType.MY_TURN))
                    emitEffect(GameUiEffect.TriggerHaptic(HapticType.LIGHT_TICK))
                }
            }
            is ServerEvent.OpponentUpdate -> {
                _uiState.update { state ->
                    val updated = state.opponents.map { opp ->
                        if (opp.name.equals(event.opponentName, ignoreCase = true)) {
                            opp.copy(cardCount = event.cardCount)
                        } else {
                            opp
                        }
                    }
                    state.copy(opponents = updated)
                }
            }
            is ServerEvent.DrawResult -> {
                emitEffect(GameUiEffect.PlaySound(SoundType.CARD_DRAW))
                viewModelScope.launch {
                    cardStatDao.recordCardDrawn(event.drawnCard.toWireToken())
                }
            }
            is ServerEvent.ErrorEvent -> {
                _uiState.update { it.copy(errorMessage = event.errorMessage) }
                emitEffect(GameUiEffect.PlaySound(SoundType.ERROR))
                emitEffect(GameUiEffect.TriggerHaptic(HapticType.HEAVY_WARNING))
            }
            is ServerEvent.ChatBroadcast -> {
                if (event.message.contains("ULTIMO", ignoreCase = true)) {
                    emitEffect(GameUiEffect.PlaySound(SoundType.ULTIMO_SHOUT))
                }
            }
            is ServerEvent.GameOver -> {
                turnTimerJob?.cancel()
                val isVictory = event.winnerName.equals(_uiState.value.localPlayerName, ignoreCase = true)
                _uiState.update { it.copy(gameOverWinner = event.winnerName) }

                emitEffect(if (isVictory) GameUiEffect.PlaySound(SoundType.VICTORY) else GameUiEffect.PlaySound(SoundType.DEFEAT))
                emitEffect(GameUiEffect.GameOverNav(event.winnerName))

                // Record lifetime match stats in Room DB
                viewModelScope.launch {
                    val durationSec = ((System.currentTimeMillis() - matchStartTimeMs) / 1000).coerceAtLeast(1)
                    recordMatchOutcomeUseCase(
                        localPlayerName = _uiState.value.localPlayerName,
                        winnerName = event.winnerName,
                        roomId = roomId,
                        opponentCount = _uiState.value.opponents.size,
                        cardsPlayed = _uiState.value.cardsPlayedCount,
                        matchDurationSeconds = durationSec
                    )
                }
            }
            is ServerEvent.ServerShutdown -> {
                _uiState.update { it.copy(errorMessage = "Server is shutting down in ${event.drainTimeoutSeconds}s") }
            }
            ServerEvent.Pong -> {
                // Heartbeat response acknowledged
            }
        }
    }

    fun onIntent(intent: GameUiIntent) {
        when (intent) {
            is GameUiIntent.CardClicked -> {
                if (intent.card.isWild) {
                    _uiState.update { it.copy(isWildPickerVisible = true, pendingWildCard = intent.card) }
                } else {
                    playCardInternal(intent.card, null)
                }
            }
            is GameUiIntent.CardPlayed -> {
                playCardInternal(intent.card, intent.chosenWildColor)
            }
            is GameUiIntent.WildColorSelected -> {
                val wildCard = _uiState.value.pendingWildCard
                _uiState.update { it.copy(isWildPickerVisible = false, pendingWildCard = null) }
                if (wildCard != null) {
                    playCardInternal(wildCard, intent.color)
                }
            }
            GameUiIntent.WildPickerDismissed -> {
                _uiState.update { it.copy(isWildPickerVisible = false, pendingWildCard = null) }
            }
            GameUiIntent.DrawCard -> {
                drawCardUseCase()
            }
            GameUiIntent.PassTurn -> {
                passTurnUseCase()
            }
            GameUiIntent.DeclareUltimo -> {
                val result = declareUltimoUseCase(_uiState.value.hand.size)
                if (result.isSuccess) {
                    _uiState.update { it.copy(isUltimoDeclared = true) }
                    emitEffect(GameUiEffect.PlaySound(SoundType.ULTIMO_SHOUT))
                    emitEffect(GameUiEffect.TriggerHaptic(HapticType.HEAVY_WARNING))
                } else {
                    _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
                }
            }
            is GameUiIntent.SendChat -> {
                // Future chat expansion
            }
            GameUiIntent.DismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
            GameUiIntent.LeaveRoom -> {
                webSocketEngine.disconnect("Player exited room")
            }
        }
    }

    private fun playCardInternal(card: Card, chosenWildColor: CardColor?) {
        val result = playCardUseCase(
            card = card,
            chosenWildColor = chosenWildColor,
            topCard = _uiState.value.topCard,
            activeColor = _uiState.value.activeColor
        )

        if (result.isSuccess) {
            // Optimistic card reduction
            _uiState.update { state ->
                val newHand = state.hand.toMutableList().apply { remove(card) }
                state.copy(
                    hand = newHand,
                    cardsPlayedCount = state.cardsPlayedCount + 1
                )
            }
            emitEffect(GameUiEffect.PlaySound(SoundType.CARD_PLAY))
            emitEffect(GameUiEffect.TriggerHaptic(HapticType.MEDIUM_SNAP))

            viewModelScope.launch {
                cardStatDao.recordCardPlayed(card.toWireToken())
            }
        } else {
            _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            emitEffect(GameUiEffect.PlaySound(SoundType.ERROR))
            emitEffect(GameUiEffect.TriggerHaptic(HapticType.HEAVY_WARNING))
        }
    }

    private fun startTurnCountdown(timeLimitMs: Long) {
        turnTimerJob?.cancel()
        turnTimerJob = viewModelScope.launch {
            var remaining = timeLimitMs
            while (isActive && remaining > 0) {
                delay(100L)
                remaining = (remaining - 100L).coerceAtLeast(0L)
                _uiState.update { it.copy(turnTimeRemainingMs = remaining) }
            }
        }
    }

    private fun emitEffect(effect: GameUiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }

    override fun onCleared() {
        super.onCleared()
        turnTimerJob?.cancel()
    }
}
