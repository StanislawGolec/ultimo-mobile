package pl.ultimo.mobile.core.network

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import pl.ultimo.mobile.core.protocol.ClientCommand
import pl.ultimo.mobile.core.protocol.ServerEvent

/**
 * Core interface managing persistent RFC 6455 WebSocket communication,
 * incoming event streaming, outgoing command dispatch, and resilient session recovery.
 */
interface WebSocketEngine {

    val connectionStatus: StateFlow<ConnectionStatus>

    val events: SharedFlow<ServerEvent>

    val errors: SharedFlow<String>

    fun connect(url: String, username: String, roomId: String? = null, accessCode: String? = null)

    fun sendCommand(command: ClientCommand): Boolean

    fun disconnect(reason: String = "Normal closure")

    fun reconnectNow()
}
