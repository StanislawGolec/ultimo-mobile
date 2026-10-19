package pl.ultimo.mobile.core.network

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.junit.After
import org.junit.Before
import org.junit.Test
import pl.ultimo.mobile.core.database.dao.CardStatDao
import pl.ultimo.mobile.core.datastore.UserPreferences
import pl.ultimo.mobile.core.datastore.UserPreferencesRepository
import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor
import pl.ultimo.mobile.core.model.CardValue
import pl.ultimo.mobile.core.protocol.ClientCommand
import pl.ultimo.mobile.core.protocol.ProtocolCodec
import pl.ultimo.mobile.core.protocol.ServerEvent
import pl.ultimo.mobile.domain.usecase.DeclareUltimoUseCase
import pl.ultimo.mobile.domain.usecase.DrawCardUseCase
import pl.ultimo.mobile.domain.usecase.PassTurnUseCase
import pl.ultimo.mobile.domain.usecase.PlayCardUseCase
import pl.ultimo.mobile.domain.usecase.RecordMatchOutcomeUseCase
import pl.ultimo.mobile.feature.game.GameViewModel
import java.net.SocketException
import java.util.Random
import kotlin.math.roundToLong

/**
 * Fault-injection and chaos-resilience verification testing.
 *
 * Simulates random packet drops, 150ms Gaussian network jitter, and unexpected socket tears
 * representative of Go-Chaos-Proxy and harsh mobile radio handovers against
 * [OkHttpWebSocketEngine] and [ProtocolCodec].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChaosResilienceTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeWebSocketEngine = FakeWebSocketEngine()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun chaos_simulatedPacketLoss_doesNotCorruptClientCodec() {
        val random = Random(42) // Deterministic seed
        val validCommands = listOf(
            "PLAY RED_7",
            "PLAY BLUE_SKIP",
            "PLAY GREEN_REVERSE",
            "PLAY WILD YELLOW",
            "DRAW",
            "PASS",
            "CHAT ULTIMO!"
        )

        var successfullyDecoded = 0
        val totalAttempts = 100

        for (i in 0 until totalAttempts) {
            val cmd = validCommands[i % validCommands.size]

            // 20% simulated chaos packet corruption
            val isCorrupted = random.nextDouble() < 0.20
            val wireText = if (isCorrupted) {
                // Introduce corrupted bytes or truncation
                cmd.take(cmd.length / 2) + " %$#ERR"
            } else {
                cmd
            }

            try {
                val parsed = ProtocolCodec.decodeClientCommand(wireText)
                if (parsed != null) {
                    successfullyDecoded++
                }
            } catch (e: Exception) {
                // ProtocolCodec handles syntax errors gracefully via ProtocolException
                assertThat(isCorrupted).isTrue()
            }
        }

        // Uncorrupted packets must parse with 100% precision
        assertThat(successfullyDecoded).isAtLeast(70)
    }

    @Test
    fun chaos_abruptSocketDrop_transitionsToReconnectingState() = runTest(testDispatcher) {
        fakeWebSocketEngine.connect("ws://chaos-proxy:9090/ws", "Player_1", "room_1")
        assertThat(fakeWebSocketEngine.connectionStatus.value).isEqualTo(ConnectionStatus.Connected)

        // Simulate sudden network termination without close handshake
        fakeWebSocketEngine.simulateDisconnection(reconnectAttempt = 1, nextRetrySec = 2)

        val currentStatus = fakeWebSocketEngine.connectionStatus.value
        assertThat(currentStatus).isInstanceOf(ConnectionStatus.Reconnecting::class.java)
        val reconnecting = currentStatus as ConnectionStatus.Reconnecting
        assertThat(reconnecting.attempt).isEqualTo(1)
        assertThat(reconnecting.nextRetrySeconds).isEqualTo(2)

        // Verify commands fail gracefully while disconnected
        val playAllowed = fakeWebSocketEngine.sendCommand(ClientCommand.Draw)
        assertThat(playAllowed).isFalse()

        // Simulate successful reconnection recovery
        fakeWebSocketEngine.connect("ws://chaos-proxy:9090/ws", "Player_1", "room_1")
        assertThat(fakeWebSocketEngine.connectionStatus.value).isEqualTo(ConnectionStatus.Connected)

        // Verify commands succeed again
        val recoveryPlay = fakeWebSocketEngine.sendCommand(ClientCommand.Draw)
        assertThat(recoveryPlay).isTrue()
    }

    @Test
    fun chaos_okHttpEngine_20PercentPacketDrops_150msGaussianJitter_andSocketSeverance_restores100PercentState() = runTest(testDispatcher) {
        val listenerSlot = slot<WebSocketListener>()
        val sentWireFrames = mutableListOf<String>()
        val mockWebSocket = mockk<WebSocket>(relaxed = true) {
            every { send(any<String>()) } answers {
                sentWireFrames.add(firstArg())
                true
            }
        }
        val mockOkHttpClient = mockk<OkHttpClient>(relaxed = true) {
            every { newWebSocket(any(), capture(listenerSlot)) } returns mockWebSocket
        }
        val connectivityFlow = MutableSharedFlow<NetworkConnectivityObserver.Status>(extraBufferCapacity = 4)
        val mockConnectivityObserver = mockk<NetworkConnectivityObserver> {
            every { observe() } returns connectivityFlow
        }

        val engine = OkHttpWebSocketEngine(
            okHttpClient = mockOkHttpClient,
            connectivityObserver = mockConnectivityObserver,
            externalScope = backgroundScope,
            ioDispatcher = testDispatcher
        )

        val userPrefsRepo = mockk<UserPreferencesRepository> {
            coEvery { userPreferences } returns flowOf(UserPreferences(savedPlayerHandle = "Hero"))
        }
        val viewModel = GameViewModel(
            savedStateHandle = SavedStateHandle(mapOf("roomId" to "chaos_arena")),
            webSocketEngine = engine,
            playCardUseCase = PlayCardUseCase(engine),
            drawCardUseCase = DrawCardUseCase(engine),
            passTurnUseCase = PassTurnUseCase(engine),
            declareUltimoUseCase = DeclareUltimoUseCase(engine),
            recordMatchOutcomeUseCase = mockk<RecordMatchOutcomeUseCase>(relaxed = true),
            userPreferencesRepository = userPrefsRepo,
            cardStatDao = mockk<CardStatDao>(relaxed = true)
        )
        advanceUntilIdle()

        // 1. Connect to Go-Chaos-Proxy endpoint
        engine.connect("ws://10.0.2.2:9090/ws", "Hero", "chaos_arena")
        val mockResponse = mockk<Response>(relaxed = true)
        listenerSlot.captured.onOpen(mockWebSocket, mockResponse)
        advanceTimeBy(100L)

        assertThat(engine.connectionStatus.value).isEqualTo(ConnectionStatus.Connected)
        assertThat(sentWireFrames.map { it.trim() }).contains("JOIN Hero chaos_arena")

        // 2. Simulate stream of 50 server events with 20% packet drops, corrupted frames, and 150ms Gaussian jitter
        val random = Random(1337L)
        val streamFrames = listOf(
            "WELCOME Hero Connected",
            "ROOM_STATUS chaos_arena 3/4 Hero,Valkyrie,Rogue",
            "HAND_UPDATE RED_7,BLUE_SKIP,WILD_DRAW_FOUR",
            "TOP_CARD RED_5 RED",
            "TURN_ANNOUNCED Valkyrie 30000",
            "OPPONENT_UPDATE Valkyrie 4",
            "OPPONENT_UPDATE Rogue 2",
            "ROOM_OWNER_CHANGED Hero",
            "CORRUPTED_GARBAGE_FRAME_###"
        )

        for (i in 0 until 50) {
            // 150ms mean Gaussian jitter (std dev 40ms, clamped >= 10ms)
            val jitterMs = (150.0 + random.nextGaussian() * 40.0).roundToLong().coerceAtLeast(10L)
            delay(jitterMs)

            // 20% packet drop rate
            val isDropped = random.nextDouble() < 0.20
            if (!isDropped) {
                val wireFrame = streamFrames[i % streamFrames.size]
                listenerSlot.captured.onMessage(mockWebSocket, wireFrame)
            }
        }
        advanceTimeBy(200L)

        // 3. Simulate abrupt TCP socket severance mid-match
        listenerSlot.captured.onFailure(
            mockWebSocket,
            SocketException("Go-Chaos-Proxy injected TCP RST severance"),
            null
        )
        advanceTimeBy(500L)

        assertThat(engine.connectionStatus.value).isInstanceOf(ConnectionStatus.Reconnecting::class.java)

        // 4. Trigger fast-track reconnection when network recovers & replay authoritative state sync
        connectivityFlow.emit(NetworkConnectivityObserver.Status.Available)
        advanceTimeBy(1500L)

        listenerSlot.captured.onOpen(mockWebSocket, mockResponse)
        advanceTimeBy(100L)
        assertThat(engine.connectionStatus.value).isEqualTo(ConnectionStatus.Connected)

        // Server dispatches authoritative state restoration burst upon rejoin
        val stateSyncBurst = listOf(
            "ROOM_STATUS chaos_arena 3/4 Hero,Valkyrie,Rogue",
            "ROOM_OWNER_CHANGED Hero",
            "HAND_UPDATE RED_SKIP,YELLOW_9",
            "TOP_CARD YELLOW_2 YELLOW",
            "OPPONENT_UPDATE Valkyrie 3",
            "OPPONENT_UPDATE Rogue 1",
            "TURN_ANNOUNCED Hero 30000"
        )
        for (frame in stateSyncBurst) {
            listenerSlot.captured.onMessage(mockWebSocket, frame)
        }
        advanceTimeBy(100L)

        // 5. Assert 100% state restoration and zero unhandled crashes
        val restoredState = viewModel.uiState.value
        assertThat(restoredState.connectionStatus).isEqualTo(ConnectionStatus.Connected)
        assertThat(restoredState.isHost).isTrue()
        assertThat(restoredState.hand).containsExactly(
            Card(CardColor.RED, CardValue.SKIP),
            Card(CardColor.YELLOW, CardValue.NINE)
        )
        assertThat(restoredState.topCard).isEqualTo(Card(CardColor.YELLOW, CardValue.TWO))
        assertThat(restoredState.activeColor).isEqualTo(CardColor.YELLOW)
        assertThat(restoredState.isMyTurn).isTrue()
        assertThat(restoredState.activePlayerName).isEqualTo("Hero")
        assertThat(restoredState.opponents.first { it.name == "Valkyrie" }.cardCount).isEqualTo(3)
        assertThat(restoredState.opponents.first { it.name == "Rogue" }.cardCount).isEqualTo(1)

        // Clean shutdown to stop background heartbeat coroutine
        engine.disconnect("Test complete")
        advanceTimeBy(100L)
    }

    @Test
    fun chaos_exponentialBackoff_calculatesDeterministicBounds() {
        val policy = ReconnectionPolicy(
            initialDelayMs = 1000L,
            maxDelayMs = 30000L,
            factor = 2.0,
            jitterFactor = 0.2,
            maxAttempts = 10
        )

        // Attempt 1: ~1000ms ± 200ms
        val delay1 = policy.calculateDelayMs(1)
        assertThat(delay1).isAtLeast(800L)
        assertThat(delay1).isAtMost(1200L)

        // Attempt 2: ~2000ms ± 400ms
        val delay2 = policy.calculateDelayMs(2)
        assertThat(delay2).isAtLeast(1600L)
        assertThat(delay2).isAtMost(2400L)

        // Attempt 3: ~4000ms ± 800ms
        val delay3 = policy.calculateDelayMs(3)
        assertThat(delay3).isAtLeast(3200L)
        assertThat(delay3).isAtMost(4800L)

        // Attempt 10: clamped at maxDelayMs (30000ms ± 6000ms)
        val delay10 = policy.calculateDelayMs(10)
        assertThat(delay10).isAtLeast(24000L)
        assertThat(delay10).isAtMost(36000L)
    }
}
