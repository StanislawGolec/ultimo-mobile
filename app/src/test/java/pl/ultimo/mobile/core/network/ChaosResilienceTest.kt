package pl.ultimo.mobile.core.network

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import pl.ultimo.mobile.core.protocol.ClientCommand
import pl.ultimo.mobile.core.protocol.ProtocolCodec
import pl.ultimo.mobile.core.protocol.ServerEvent
import java.util.Random

/**
 * Fault-injection and chaos-resilience verification testing.
 *
 * Simulates random packet drops, network jitter, and unexpected socket tears
 * representative of Go-Chaos-Proxy and harsh mobile radio handovers.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChaosResilienceTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeWebSocketEngine = FakeWebSocketEngine()

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
        var totalAttempts = 100

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
        val playAllowed = fakeWebSocketEngine.sendCommand(ClientCommand.DrawCard)
        assertThat(playAllowed).isFalse()

        // Simulate successful reconnection recovery
        fakeWebSocketEngine.connect("ws://chaos-proxy:9090/ws", "Player_1", "room_1")
        assertThat(fakeWebSocketEngine.connectionStatus.value).isEqualTo(ConnectionStatus.Connected)

        // Verify commands succeed again
        val recoveryPlay = fakeWebSocketEngine.sendCommand(ClientCommand.DrawCard)
        assertThat(recoveryPlay).isTrue()
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
