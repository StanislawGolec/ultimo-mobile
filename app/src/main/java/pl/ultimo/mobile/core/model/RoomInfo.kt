package pl.ultimo.mobile.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class RoomInfo(
    val roomId: String,
    val name: String,
    val currentPlayers: Int = 0,
    val maxPlayers: Int = 4,
    val status: String = "WAITING",
    val host: String = ""
)
