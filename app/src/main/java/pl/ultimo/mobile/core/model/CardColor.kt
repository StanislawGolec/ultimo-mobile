package pl.ultimo.mobile.core.model

import androidx.compose.runtime.Immutable

@Immutable
enum class CardColor {
    RED,
    BLUE,
    GREEN,
    YELLOW,
    WILD;

    val isStandardColor: Boolean
        get() = this != WILD
}
