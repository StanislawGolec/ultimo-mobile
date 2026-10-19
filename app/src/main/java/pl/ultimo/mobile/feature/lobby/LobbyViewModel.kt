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
import pl.ultimo.mobile.core.model.RoomInfo
import pl.ultimo.mobile.core.network.ConnectionStatus
import pl.ultimo.mobile.core.network.WebSocketEngine
import pl.ultimo.mobile.core.protocol.ClientCommand
import pl.ultimo.mobile.core.protocol.ServerEvent
import pl.ultimo.mobile.domain.usecase.ConnectToGameUseCase
import javax.inject.Inject

@Immutable
data class LobbyUiState(
    val username: String = "Player_1",
    val roomId: String = "room_1",
    val serverUrl: String = UserPreferences.DEFAULT_SERVER_URL,
    val useChaosProxy: Boolean = false,
    val chaosProxyUrl: String = UserPreferences.DEFAULT_CHAOS_PROXY_URL,
    val connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected,
    val nameTakenError: String? = null,
    val activeRooms: List<RoomInfo> = emptyList(),
    val isRefreshingRooms: Boolean = false,
    val isRoomBrowserOpen: Boolean = false,
    val isCreateRoomOpen: Boolean = false,
    val pendingPrivateRoom: RoomInfo? = null
) {
    val effectiveUrl: String
        get() = if (useChaosProxy) chaosProxyUrl else serverUrl

    val isUsernameValid: Boolean
        get() = LobbyViewModel.isValidUsername(username)

    val validationErrorMessage: String?
        get() = when {
            username.length < 2 -> "Handle must be at least 2 characters"
            username.length > 24 -> "Handle must not exceed 24 characters"
            username.contains(" ") -> "Handle must not contain spaces"
            username.any { it < ' ' } -> "Handle must not contain control characters or newlines"
            !isUsernameValid -> "Handle contains disallowed characters"
            else -> null
        }
}

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val connectToGameUseCase: ConnectToGameUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val webSocketEngine: WebSocketEngine
) : ViewModel() {

    companion object {
        val USERNAME_REGEX = Regex("^[a-zA-Z0-9\\u00C0-\\u024F!@#$%^&*()\\-_=+\\[\\]{}:;\"'<>,.?/~]{2,24}$")

        fun isValidUsername(name: String): Boolean {
            return USERNAME_REGEX.matches(name)
        }
    }

    private data class CustomInputs(
        val username: String = "Player_1",
        val roomId: String = "room_1",
        val isInitialized: Boolean = false,
        val nameTakenError: String? = null,
        val isRoomBrowserOpen: Boolean = false,
        val isCreateRoomOpen: Boolean = false,
        val pendingPrivateRoom: RoomInfo? = null
    )

    private val _customInputs = MutableStateFlow(CustomInputs())
    private val _activeRooms = MutableStateFlow<List<RoomInfo>>(emptyList())
    private val _isRefreshingRooms = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            webSocketEngine.events.collect { event ->
                when (event) {
                    is ServerEvent.ErrorEvent -> {
                        if (event.errorMessage.contains("NAME_TAKEN", ignoreCase = true)) {
                            val currentName = _customInputs.value.username
                            _customInputs.update {
                                it.copy(nameTakenError = "Username '$currentName' is already taken in this room. Please pick another handle.")
                            }
                        }
                    }
                    is ServerEvent.RoomList -> {
                        _activeRooms.update { event.rooms }
                        _isRefreshingRooms.update { false }
                    }
                    else -> Unit
                }
            }
        }
    }

    val uiState: StateFlow<LobbyUiState> = combine(
        userPreferencesRepository.userPreferences,
        webSocketEngine.connectionStatus,
        _customInputs,
        _activeRooms,
        _isRefreshingRooms
    ) { prefs, status, inputs, rooms, isRefreshing ->
        val finalUsername = if (inputs.isInitialized) inputs.username else prefs.savedPlayerHandle
        LobbyUiState(
            username = finalUsername,
            roomId = inputs.roomId,
            serverUrl = prefs.serverUrl,
            useChaosProxy = prefs.useChaosProxy,
            chaosProxyUrl = prefs.chaosProxyUrl,
            connectionStatus = status,
            nameTakenError = inputs.nameTakenError,
            activeRooms = rooms,
            isRefreshingRooms = isRefreshing,
            isRoomBrowserOpen = inputs.isRoomBrowserOpen,
            isCreateRoomOpen = inputs.isCreateRoomOpen,
            pendingPrivateRoom = inputs.pendingPrivateRoom
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LobbyUiState()
    )

    fun onUsernameChanged(name: String) {
        _customInputs.update {
            it.copy(
                username = name,
                isInitialized = true,
                nameTakenError = null
            )
        }
        viewModelScope.launch {
            userPreferencesRepository.updateSavedPlayerHandle(name)
        }
    }

    fun onRoomIdChanged(room: String) {
        _customInputs.update { it.copy(roomId = room, isInitialized = true) }
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

    fun clearNameTakenError() {
        _customInputs.update { it.copy(nameTakenError = null) }
    }

    fun openRoomBrowser() {
        _customInputs.update { it.copy(isRoomBrowserOpen = true) }
        refreshRooms()
    }

    fun closeRoomBrowser() {
        _customInputs.update { it.copy(isRoomBrowserOpen = false) }
    }

    fun refreshRooms() {
        _isRefreshingRooms.update { true }
        viewModelScope.launch {
            val current = uiState.value
            connectToGameUseCase(
                url = current.effectiveUrl,
                username = current.username,
                roomId = current.roomId
            )
            webSocketEngine.sendCommand(ClientCommand.ListRooms)
        }
    }

    fun openCreateRoom() {
        _customInputs.update { it.copy(isCreateRoomOpen = true) }
    }

    fun closeCreateRoom() {
        _customInputs.update { it.copy(isCreateRoomOpen = false) }
    }

    fun createRoom(
        name: String,
        visibility: String,
        accessCode: String?,
        capacity: Int,
        onCreated: (roomId: String) -> Unit
    ) {
        val current = uiState.value
        viewModelScope.launch {
            connectToGameUseCase(
                url = current.effectiveUrl,
                username = current.username,
                roomId = name
            )
            webSocketEngine.sendCommand(
                ClientCommand.CreateRoom(
                    name = name,
                    visibility = visibility,
                    accessCode = accessCode,
                    capacity = capacity
                )
            )
            closeCreateRoom()
            onCreated(name)
        }
    }

    fun requestJoinRoom(room: RoomInfo, onJoined: (roomId: String) -> Unit) {
        if (room.status.contains("PRIVATE", ignoreCase = true)) {
            _customInputs.update { it.copy(pendingPrivateRoom = room) }
        } else {
            joinRoom(room.roomId, null, onJoined)
        }
    }

    fun joinRoom(roomId: String, accessCode: String? = null, onJoined: (roomId: String) -> Unit) {
        val current = uiState.value
        viewModelScope.launch {
            connectToGameUseCase(
                url = current.effectiveUrl,
                username = current.username,
                roomId = roomId
            )
            if (!accessCode.isNullOrBlank()) {
                webSocketEngine.sendCommand(ClientCommand.Join(current.username, roomId, accessCode))
            }
            _customInputs.update { it.copy(pendingPrivateRoom = null) }
            closeRoomBrowser()
            onJoined(roomId)
        }
    }

    fun dismissAccessCodeDialog() {
        _customInputs.update { it.copy(pendingPrivateRoom = null) }
    }

    fun joinMatch(onJoined: (roomId: String) -> Unit) {
        val current = uiState.value
        if (!current.isUsernameValid) return
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
