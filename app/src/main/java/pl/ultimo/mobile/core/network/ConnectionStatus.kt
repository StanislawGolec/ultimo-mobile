package pl.ultimo.mobile.core.network

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ConnectionStatus {
    data object Disconnected : ConnectionStatus
    data object Connecting : ConnectionStatus
    data object Connected : ConnectionStatus
    data class Reconnecting(val attempt: Int, val nextRetrySeconds: Int) : ConnectionStatus
    data class Failed(val error: String) : ConnectionStatus
}
