package pl.ultimo.mobile.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.ultimo.mobile.core.protocol.ClientCommand
import pl.ultimo.mobile.core.protocol.ServerEvent

/**
 * High-fidelity test double for [WebSocketEngine].
 * Allows deterministic simulation of server events, disconnections, and chaos fault injection.
 */
class FakeWebSocketEngine : WebSocketEngine {

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    override val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _events = MutableSharedFlow<ServerEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<ServerEvent> = _events.asSharedFlow()

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 64)
    override val errors: SharedFlow<String> = _errors.asSharedFlow()

    val sentCommands = mutableListOf<ClientCommand>()
    var lastConnectedUrl: String? = null
    var lastUsername: String? = null
    var lastRoomId: String? = null
    var lastAccessCode: String? = null
    var isConnected: Boolean = true
    var reconnectNowCalledCount: Int = 0

    override fun connect(url: String, username: String, roomId: String?, accessCode: String?) {
        lastConnectedUrl = url
        lastUsername = username
        lastRoomId = roomId
        lastAccessCode = accessCode
        isConnected = true
        _connectionStatus.value = ConnectionStatus.Connected
    }

    override fun reconnectNow() {
        reconnectNowCalledCount++
        if (lastConnectedUrl != null && lastUsername != null) {
            connect(lastConnectedUrl!!, lastUsername!!, lastRoomId, lastAccessCode)
        }
    }

    override fun sendCommand(command: ClientCommand): Boolean {
        sentCommands.add(command)
        return isConnected
    }

    override fun disconnect(reason: String) {
        isConnected = false
        _connectionStatus.value = ConnectionStatus.Disconnected
    }

    suspend fun emitEvent(event: ServerEvent) {
        _events.emit(event)
    }

    fun setStatus(status: ConnectionStatus) {
        _connectionStatus.value = status
    }

    suspend fun emitError(message: String) {
        _errors.emit(message)
    }

    fun simulateDisconnection(reconnectAttempt: Int = 1, nextRetrySec: Int = 3) {
        isConnected = false
        _connectionStatus.value = ConnectionStatus.Reconnecting(reconnectAttempt, nextRetrySec)
    }
}
