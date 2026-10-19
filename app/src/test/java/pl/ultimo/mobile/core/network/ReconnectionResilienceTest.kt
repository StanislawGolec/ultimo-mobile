package pl.ultimo.mobile.core.network

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import pl.ultimo.mobile.core.database.dao.CardStatDao
import pl.ultimo.mobile.core.datastore.UserPreferences
import pl.ultimo.mobile.core.datastore.UserPreferencesRepository
import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor
import pl.ultimo.mobile.core.model.CardValue
import pl.ultimo.mobile.core.protocol.ServerEvent
import pl.ultimo.mobile.domain.usecase.DeclareUltimoUseCase
import pl.ultimo.mobile.domain.usecase.DrawCardUseCase
import pl.ultimo.mobile.domain.usecase.PassTurnUseCase
import pl.ultimo.mobile.domain.usecase.PlayCardUseCase
import pl.ultimo.mobile.domain.usecase.RecordMatchOutcomeUseCase
import pl.ultimo.mobile.feature.game.GameViewModel
import pl.ultimo.mobile.feature.game.model.GameUiIntent
import pl.ultimo.mobile.feature.game.model.OpponentUiModel

/**
 * Reconnection resilience and 120s grace period verification tests (Stage 13).
 *
 * Validates:
 * 1. 120-second disconnect grace period computation and expiration.
 * 2. Session credential caching (username, roomId, accessCode).
 * 3. Connection drop transitions to [ConnectionStatus.Reconnecting].
 * 4. Immediate reconnection trigger via [WebSocketEngine.reconnectNow].
 * 5. Snapshot state recovery ([ServerEvent.HandUpdate], [ServerEvent.TopCard],
 *    [ServerEvent.TurnAnnounced], [ServerEvent.OpponentUpdate]) without clearing local state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReconnectionResilienceTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeWebSocketEngine = FakeWebSocketEngine()
    private val userPreferencesRepository = mockk<UserPreferencesRepository>()
    private val cardStatDao = mockk<CardStatDao>(relaxed = true)
    private val recordMatchOutcomeUseCase = mockk<RecordMatchOutcomeUseCase>(relaxed = true)

    private lateinit var playCardUseCase: PlayCardUseCase
    private lateinit var drawCardUseCase: DrawCardUseCase
    private lateinit var passTurnUseCase: PassTurnUseCase
    private lateinit var declareUltimoUseCase: DeclareUltimoUseCase
    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { userPreferencesRepository.userPreferences } returns flowOf(
            UserPreferences(savedPlayerHandle = "Hero")
        )

        playCardUseCase = PlayCardUseCase(fakeWebSocketEngine)
        drawCardUseCase = DrawCardUseCase(fakeWebSocketEngine)
        passTurnUseCase = PassTurnUseCase(fakeWebSocketEngine)
        declareUltimoUseCase = DeclareUltimoUseCase(fakeWebSocketEngine)

        val savedStateHandle = SavedStateHandle(mapOf("roomId" to "arena_120"))

        viewModel = GameViewModel(
            savedStateHandle = savedStateHandle,
            webSocketEngine = fakeWebSocketEngine,
            playCardUseCase = playCardUseCase,
            drawCardUseCase = drawCardUseCase,
            passTurnUseCase = passTurnUseCase,
            declareUltimoUseCase = declareUltimoUseCase,
            recordMatchOutcomeUseCase = recordMatchOutcomeUseCase,
            userPreferencesRepository = userPreferencesRepository,
            cardStatDao = cardStatDao
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun reconnectionPolicy_verifies120sGracePeriodBounds() {
        val policy = ReconnectionPolicy(gracePeriodMs = 120_000L)
        val disconnectTime = 1_000_000L

        // After 60 seconds (halfway): within grace period
        val at60s = disconnectTime + 60_000L
        assertThat(policy.isWithinGracePeriod(disconnectTime, at60s)).isTrue()
        assertThat(policy.remainingGracePeriodSeconds(disconnectTime, at60s)).isEqualTo(60)

        // At 119 seconds: still valid
        val at119s = disconnectTime + 119_000L
        assertThat(policy.isWithinGracePeriod(disconnectTime, at119s)).isTrue()
        assertThat(policy.remainingGracePeriodSeconds(disconnectTime, at119s)).isEqualTo(1)

        // At 120 seconds: expired
        val at120s = disconnectTime + 120_000L
        assertThat(policy.isWithinGracePeriod(disconnectTime, at120s)).isFalse()
        assertThat(policy.remainingGracePeriodSeconds(disconnectTime, at120s)).isEqualTo(0)

        // Beyond 120 seconds: expired
        val at150s = disconnectTime + 150_000L
        assertThat(policy.isWithinGracePeriod(disconnectTime, at150s)).isFalse()
        assertThat(policy.remainingGracePeriodSeconds(disconnectTime, at150s)).isEqualTo(0)
    }

    @Test
    fun webSocketEngine_cachesSessionCredentials_andRestoresOnReconnectNow() {
        fakeWebSocketEngine.connect(
            url = "ws://game-server:8080/ws",
            username = "Hero",
            roomId = "arena_120",
            accessCode = "secret99"
        )

        assertThat(fakeWebSocketEngine.lastConnectedUrl).isEqualTo("ws://game-server:8080/ws")
        assertThat(fakeWebSocketEngine.lastUsername).isEqualTo("Hero")
        assertThat(fakeWebSocketEngine.lastRoomId).isEqualTo("arena_120")
        assertThat(fakeWebSocketEngine.lastAccessCode).isEqualTo("secret99")
        assertThat(fakeWebSocketEngine.isConnected).isTrue()

        // Trigger manual ReconnectNow
        fakeWebSocketEngine.reconnectNow()

        assertThat(fakeWebSocketEngine.reconnectNowCalledCount).isEqualTo(1)
        assertThat(fakeWebSocketEngine.isConnected).isTrue()
        assertThat(fakeWebSocketEngine.lastUsername).isEqualTo("Hero")
        assertThat(fakeWebSocketEngine.lastRoomId).isEqualTo("arena_120")
        assertThat(fakeWebSocketEngine.lastAccessCode).isEqualTo("secret99")
    }

    @Test
    fun accidentalDisconnect_transitionsToReconnecting_preservesExistingGameState() = runTest(testDispatcher) {
        advanceUntilIdle()

        // Set initial game table state
        fakeWebSocketEngine.emitEvent(
            ServerEvent.HandUpdate(
                listOf(
                    Card(CardColor.RED, CardValue.SEVEN),
                    Card(CardColor.BLUE, CardValue.SKIP)
                )
            )
        )
        fakeWebSocketEngine.emitEvent(
            ServerEvent.TopCard(Card(CardColor.RED, CardValue.FIVE), CardColor.RED)
        )
        fakeWebSocketEngine.emitEvent(
            ServerEvent.TurnAnnounced("Hero", 30000L)
        )
        advanceUntilIdle()

        // Verify initial state populated
        assertThat(viewModel.uiState.value.hand).hasSize(2)
        assertThat(viewModel.uiState.value.topCard).isEqualTo(Card(CardColor.RED, CardValue.FIVE))
        assertThat(viewModel.uiState.value.isMyTurn).isTrue()

        // Simulate sudden network drop
        fakeWebSocketEngine.simulateDisconnection(reconnectAttempt = 1, nextRetrySec = 3)
        advanceUntilIdle()

        // Verify connection status is Reconnecting
        val status = viewModel.uiState.value.connectionStatus
        assertThat(status).isInstanceOf(ConnectionStatus.Reconnecting::class.java)
        val reconnecting = status as ConnectionStatus.Reconnecting
        assertThat(reconnecting.attempt).isEqualTo(1)
        assertThat(reconnecting.nextRetrySeconds).isEqualTo(3)

        // CRUCIAL: Match state must be preserved across disconnection
        assertThat(viewModel.uiState.value.hand).hasSize(2)
        assertThat(viewModel.uiState.value.topCard).isEqualTo(Card(CardColor.RED, CardValue.FIVE))
        assertThat(viewModel.uiState.value.isMyTurn).isTrue()
    }

    @Test
    fun socketReconnection_consumesRecoverySnapshot_withoutClearingState() = runTest(testDispatcher) {
        advanceUntilIdle()

        // Simulate disconnection
        fakeWebSocketEngine.simulateDisconnection(reconnectAttempt = 2, nextRetrySec = 1)
        advanceUntilIdle()

        // Server recovery snapshot arrives upon reconnect
        fakeWebSocketEngine.emitEvent(
            ServerEvent.HandUpdate(
                listOf(
                    Card(CardColor.GREEN, CardValue.ONE),
                    Card(CardColor.GREEN, CardValue.TWO),
                    Card(CardColor.YELLOW, CardValue.REVERSE)
                )
            )
        )
        fakeWebSocketEngine.emitEvent(
            ServerEvent.TopCard(Card(CardColor.GREEN, CardValue.ZERO), CardColor.GREEN)
        )
        fakeWebSocketEngine.emitEvent(
            ServerEvent.TurnAnnounced("Opponent1", 28000L)
        )
        fakeWebSocketEngine.emitEvent(
            ServerEvent.OpponentUpdate(opponentName = "Opponent1", cardCount = 4)
        )
        fakeWebSocketEngine.emitEvent(
            ServerEvent.OpponentUpdate(opponentName = "Opponent2", cardCount = 6)
        )
        advanceUntilIdle()

        // Verify updated recovery snapshot applied smoothly
        val state = viewModel.uiState.value
        assertThat(state.hand).hasSize(3)
        assertThat(state.topCard).isEqualTo(Card(CardColor.GREEN, CardValue.ZERO))
        assertThat(state.activeColor).isEqualTo(CardColor.GREEN)
        assertThat(state.activePlayerName).isEqualTo("Opponent1")
        assertThat(state.isMyTurn).isFalse()
        assertThat(state.opponents).hasSize(2)
        assertThat(state.opponents[0].name).isEqualTo("Opponent1")
        assertThat(state.opponents[0].cardCount).isEqualTo(4)
    }

    @Test
    fun reconnectNowIntent_invokesEngineReconnectNow() = runTest(testDispatcher) {
        advanceUntilIdle()

        fakeWebSocketEngine.connect("ws://game:8080/ws", "Hero", "room_1")
        viewModel.onIntent(GameUiIntent.ReconnectNow)
        advanceUntilIdle()

        assertThat(fakeWebSocketEngine.reconnectNowCalledCount).isAtLeast(1)
    }
}
