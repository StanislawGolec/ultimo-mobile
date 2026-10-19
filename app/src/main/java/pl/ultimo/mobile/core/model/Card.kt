package pl.ultimo.mobile.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class Card(
    val color: CardColor,
    val value: CardValue
) {
    val isWild: Boolean
        get() = color == CardColor.WILD || value.isWild

    fun canPlayOn(
        topCard: Card?,
        activeColor: CardColor?,
        pendingSkipDeflection: Boolean = false,
        pendingDrawPenalty: Int = 0
    ): Boolean {
        if (pendingSkipDeflection) {
            return value == CardValue.SKIP
        }
        if (pendingDrawPenalty > 0) {
            return when (topCard?.value) {
                CardValue.DRAW_TWO -> value == CardValue.DRAW_TWO || value == CardValue.WILD_DRAW_FOUR
                CardValue.WILD_DRAW_FOUR -> value == CardValue.WILD_DRAW_FOUR
                else -> value == CardValue.DRAW_TWO || value == CardValue.WILD_DRAW_FOUR
            }
        }
        if (topCard == null) return true
        if (isWild) return true
        val targetColor = activeColor ?: topCard.color
        return color == targetColor || value == topCard.value
    }

    fun toWireToken(): String {
        return when (value) {
            CardValue.WILD -> "WILD"
            CardValue.WILD_DRAW_FOUR -> "WILD_DRAW_FOUR"
            else -> "${color.name}_${value.name}"
        }
    }
}
