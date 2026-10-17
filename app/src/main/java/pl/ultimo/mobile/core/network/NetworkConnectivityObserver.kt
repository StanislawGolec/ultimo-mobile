package pl.ultimo.mobile.core.network

import kotlinx.coroutines.flow.Flow

/**
 * Interface for observing device network reachability and network switch events (Wi-Fi / Cellular).
 */
interface NetworkConnectivityObserver {

    val isCurrentlyConnected: Boolean

    fun observe(): Flow<Status>

    enum class Status {
        Available,
        Unavailable,
        Losing,
        Lost
    }
}
