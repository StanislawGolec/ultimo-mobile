package pl.ultimo.mobile.core.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import pl.ultimo.mobile.core.di.ApplicationScope
import pl.ultimo.mobile.core.di.IoDispatcher
import pl.ultimo.mobile.core.protocol.ClientCommand
import pl.ultimo.mobile.core.protocol.ProtocolCodec
import pl.ultimo.mobile.core.protocol.ProtocolException
import pl.ultimo.mobile.core.protocol.ServerEvent
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OkHttpWebSocketEngine @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val connectivityObserver: NetworkConnectivityObserver,
    @ApplicationScope private val externalScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : WebSocketEngine {

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    override val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _events = MutableSharedFlow<ServerEvent>(replay = 0, extraBufferCapacity = 64)
    override val events: SharedFlow<ServerEvent> = _events.asSharedFlow()

    private val _errors = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 16)
    override val errors: SharedFlow<String> = _errors.asSharedFlow()

    private val reconnectionPolicy = ReconnectionPolicy()
    private val reconnectAttempt = AtomicInteger(0)
    private val isManualDisconnect = AtomicBoolean(false)

    // Cached session credentials for seamless re-binding within 120s grace period
    private val activeUrl = AtomicReference<String?>(null)
    private val activeUsername = AtomicReference<String?>(null)
    private val activeRoomId = AtomicReference<String?>(null)
    private val activeAccessCode = AtomicReference<String?>(null)
    private val disconnectTimestampMs = AtomicLong(0L)

    private var currentWebSocket: WebSocket? = null
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private val lastMessageOrPongTime = AtomicLong(0L)

    init {
        // Monitor device connectivity changes: trigger immediate reconnect if network returns
        externalScope.launch(ioDispatcher) {
            connectivityObserver.observe().collect { status ->
                if (status == NetworkConnectivityObserver.Status.Available &&
                    _connectionStatus.value is ConnectionStatus.Reconnecting &&
                    !isManualDisconnect.get()
                ) {
                    // Fast-track reconnect when internet returns
                    triggerReconnect(immediate = true)
                }
            }
        }
    }

    override fun connect(url: String, username: String, roomId: String?, accessCode: String?) {
        isManualDisconnect.set(false)
        reconnectAttempt.set(0)
        disconnectTimestampMs.set(0L)
        activeUrl.set(url)
        activeUsername.set(username)
        activeRoomId.set(roomId)
        activeAccessCode.set(accessCode)

        startConnection(url)
    }

    override fun reconnectNow() {
        if (isManualDisconnect.get()) return
        triggerReconnect(immediate = true)
    }

    private fun startConnection(url: String) {
        reconnectJob?.cancel()
        closeCurrentWebSocket()

        _connectionStatus.value = ConnectionStatus.Connecting

        val request = try {
            Request.Builder()
                .url(url)
                .build()
        } catch (e: Exception) {
            _connectionStatus.value = ConnectionStatus.Failed("Invalid WebSocket URL: ${e.message}")
            return
        }

        currentWebSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionStatus.value = ConnectionStatus.Connected
                reconnectAttempt.set(0)
                disconnectTimestampMs.set(0L)
                lastMessageOrPongTime.set(System.currentTimeMillis())

                // Automatic handshake: dispatch cached JOIN command upon connection/reconnection
                val username = activeUsername.get()
                if (!username.isNullOrBlank()) {
                    val joinCommand = ClientCommand.Join(
                        username = username,
                        roomId = activeRoomId.get(),
                        accessCode = activeAccessCode.get()
                    )
                    val wire = ProtocolCodec.encodeCommand(joinCommand)
                    webSocket.send(wire)
                }

                startHeartbeatWatchdog()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                lastMessageOrPongTime.set(System.currentTimeMillis())
                try {
                    val event = ProtocolCodec.decodeEvent(text)
                    externalScope.launch(ioDispatcher) {
                        _events.emit(event)
                    }
                } catch (pe: ProtocolException) {
                    externalScope.launch(ioDispatcher) {
                        _errors.emit("Protocol decode error: ${pe.message}")
                    }
                } catch (e: Exception) {
                    externalScope.launch(ioDispatcher) {
                        _errors.emit("Unexpected message error: ${e.message}")
                    }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                stopHeartbeatWatchdog()
                if (!isManualDisconnect.get()) {
                    if (disconnectTimestampMs.get() == 0L) {
                        disconnectTimestampMs.set(System.currentTimeMillis())
                    }
                    triggerReconnect(immediate = false)
                } else {
                    _connectionStatus.value = ConnectionStatus.Disconnected
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                stopHeartbeatWatchdog()
                if (!isManualDisconnect.get()) {
                    if (disconnectTimestampMs.get() == 0L) {
                        disconnectTimestampMs.set(System.currentTimeMillis())
                    }
                    triggerReconnect(immediate = false)
                } else {
                    _connectionStatus.value = ConnectionStatus.Failed(t.message ?: "Connection failure")
                }
            }
        })
    }

    override fun sendCommand(command: ClientCommand): Boolean {
        val socket = currentWebSocket ?: return false
        if (_connectionStatus.value != ConnectionStatus.Connected) return false

        val wire = ProtocolCodec.encodeCommand(command)
        return socket.send(wire)
    }

    override fun disconnect(reason: String) {
        isManualDisconnect.set(true)
        disconnectTimestampMs.set(0L)
        reconnectJob?.cancel()
        stopHeartbeatWatchdog()
        closeCurrentWebSocket(reason)
        _connectionStatus.value = ConnectionStatus.Disconnected
    }

    private fun closeCurrentWebSocket(reason: String = "Normal closure") {
        try {
            currentWebSocket?.close(1000, reason)
        } catch (ignored: Exception) {
        } finally {
            currentWebSocket = null
        }
    }

    private fun triggerReconnect(immediate: Boolean) {
        if (isManualDisconnect.get()) return

        val url = activeUrl.get() ?: return
        val disconnectTime = disconnectTimestampMs.get()

        // 120s Grace Period Watchdog
        if (disconnectTime > 0L && !reconnectionPolicy.isWithinGracePeriod(disconnectTime)) {
            _connectionStatus.value = ConnectionStatus.Failed("120s reconnect grace period expired")
            return
        }

        val attempt = reconnectAttempt.incrementAndGet()

        if (!reconnectionPolicy.canAttemptAgain(attempt)) {
            _connectionStatus.value = ConnectionStatus.Failed("Max reconnection attempts exceeded ($attempt)")
            return
        }

        val totalDelayMs = if (immediate) 0L else reconnectionPolicy.computeDelayMs(attempt)

        reconnectJob?.cancel()
        reconnectJob = externalScope.launch(ioDispatcher) {
            val totalSeconds = (totalDelayMs / 1000).toInt().coerceAtLeast(1)

            // Countdown ticker for user UI
            for (remaining in totalSeconds downTo 1) {
                if (disconnectTime > 0L && !reconnectionPolicy.isWithinGracePeriod(disconnectTime)) {
                    _connectionStatus.value = ConnectionStatus.Failed("120s reconnect grace period expired")
                    return@launch
                }
                _connectionStatus.value = ConnectionStatus.Reconnecting(attempt, remaining)
                delay(1000L)
            }

            if (!isManualDisconnect.get() && isActive) {
                startConnection(url)
            }
        }
    }

    private fun startHeartbeatWatchdog() {
        stopHeartbeatWatchdog()
        heartbeatJob = externalScope.launch(ioDispatcher) {
            while (isActive && _connectionStatus.value == ConnectionStatus.Connected) {
                delay(15_000L) // 15-second heartbeat interval

                if (!isActive) break

                // Dispatch PING
                val pingWire = ProtocolCodec.encodeCommand(ClientCommand.Ping)
                currentWebSocket?.send(pingWire)

                // Wait 5 seconds for PONG or incoming activity
                delay(5_000L)

                val elapsed = System.currentTimeMillis() - lastMessageOrPongTime.get()
                if (elapsed > 20_000L) {
                    // Socket is dead (no incoming traffic in 20s)
                    closeCurrentWebSocket("Heartbeat timeout")
                    if (disconnectTimestampMs.get() == 0L) {
                        disconnectTimestampMs.set(System.currentTimeMillis())
                    }
                    triggerReconnect(immediate = false)
                    break
                }
            }
        }
    }

    private fun stopHeartbeatWatchdog() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }
}
