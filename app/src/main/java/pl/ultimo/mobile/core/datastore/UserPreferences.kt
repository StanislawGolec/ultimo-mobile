package pl.ultimo.mobile.core.datastore

import androidx.compose.runtime.Immutable

/**
 * Immutable domain model representing user application preferences and network endpoints.
 */
@Immutable
data class UserPreferences(
    val serverUrl: String = DEFAULT_SERVER_URL,
    val useChaosProxy: Boolean = false,
    val chaosProxyUrl: String = DEFAULT_CHAOS_PROXY_URL,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val autoDeclareUltimo: Boolean = false,
    val savedPlayerHandle: String = "Player_1"
) {
    /**
     * Resolves the target WebSocket endpoint depending on whether chaos testing proxy is active.
     */
    val effectiveWebSocketUrl: String
        get() = if (useChaosProxy) chaosProxyUrl else serverUrl

    companion object {
        const val DEFAULT_SERVER_URL = "ws://10.0.2.2:8080/ws"
        const val DEFAULT_CHAOS_PROXY_URL = "ws://10.0.2.2:8090/ws"
    }
}
