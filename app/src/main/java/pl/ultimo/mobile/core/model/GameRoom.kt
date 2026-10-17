package pl.ultimo.mobile.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class GameRoom(
    val roomId: String,
    val currentPlayers: Int = 0,
    val maxPlayers: Int = 4,
    val playerNames: List<String> = emptyList(),
    val isGameInProgress: Boolean = false
)
