package pl.ultimo.mobile.core.model

import androidx.compose.runtime.Immutable

@Immutable
enum class CardValue(val pointValue: Int) {
    ZERO(0),
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    SKIP(20),
    REVERSE(20),
    DRAW_TWO(20),
    WILD(50),
    WILD_DRAW_FOUR(50);

    val isAction: Boolean
        get() = this in listOf(SKIP, REVERSE, DRAW_TWO)

    val isWild: Boolean
        get() = this == WILD || this == WILD_DRAW_FOUR
}
