package pl.ultimo.mobile.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class Player(
    val id: String,
    val name: String,
    val cardCount: Int = 0,
    val isCurrentTurn: Boolean = false,
    val isConnected: Boolean = true
)
