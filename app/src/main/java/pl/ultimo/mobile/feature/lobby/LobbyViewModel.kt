package pl.ultimo.mobile.feature.lobby

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.ultimo.mobile.core.datastore.UserPreferences
import pl.ultimo.mobile.core.datastore.UserPreferencesRepository
import pl.ultimo.mobile.core.network.ConnectionStatus
import pl.ultimo.mobile.core.network.WebSocketEngine
import pl.ultimo.mobile.domain.usecase.ConnectToGameUseCase
import javax.inject.Inject

@Immutable
data class LobbyUiState(
    val username: String = "Player_1",
    val roomId: String = "room_1",
    val serverUrl: String = UserPreferences.DEFAULT_SERVER_URL,
    val useChaosProxy: Boolean = false,
    val chaosProxyUrl: String = UserPreferences.DEFAULT_CHAOS_PROXY_URL,
    val connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected
) {
    val effectiveUrl: String
        get() = if (useChaosProxy) chaosProxyUrl else serverUrl
}

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val connectToGameUseCase: ConnectToGameUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val webSocketEngine: WebSocketEngine
) : ViewModel() {

    private val _customInputs = MutableStateFlow(
        Triple("Player_1", "room_1", false) // username, roomId, isInitialized
    )

    val uiState: StateFlow<LobbyUiState> = combine(
        userPreferencesRepository.userPreferences,
        webSocketEngine.connectionStatus,
        _customInputs
    ) { prefs, status, inputs ->
        val (usernameInput, roomInput, isInitialized) = inputs
        val finalUsername = if (isInitialized) usernameInput else prefs.savedPlayerHandle
        LobbyUiState(
            username = finalUsername,
            roomId = roomInput,
            serverUrl = prefs.serverUrl,
            useChaosProxy = prefs.useChaosProxy,
            chaosProxyUrl = prefs.chaosProxyUrl,
            connectionStatus = status
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LobbyUiState()
    )

    fun onUsernameChanged(name: String) {
        _customInputs.update { Triple(name, it.second, true) }
        viewModelScope.launch {
            userPreferencesRepository.updateSavedPlayerHandle(name)
        }
    }

    fun onRoomIdChanged(room: String) {
        _customInputs.update { Triple(it.first, room, true) }
    }

    fun onServerUrlChanged(url: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateServerUrl(url)
        }
    }

    fun onChaosProxyToggled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateChaosProxySettings(enabled)
        }
    }

    fun joinMatch(onJoined: (roomId: String) -> Unit) {
        val current = uiState.value
        viewModelScope.launch {
            connectToGameUseCase(
                url = current.effectiveUrl,
                username = current.username,
                roomId = current.roomId
            )
            onJoined(current.roomId)
        }
    }
}
