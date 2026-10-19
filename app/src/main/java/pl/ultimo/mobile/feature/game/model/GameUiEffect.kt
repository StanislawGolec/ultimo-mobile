package pl.ultimo.mobile.feature.game.model

/**
 * Single-shot side-effects triggered by the ViewModel (sounds, haptic vibrations, navigation).
 */
sealed interface GameUiEffect {
    data class PlaySound(val soundType: SoundType) : GameUiEffect
    data class TriggerHaptic(val type: HapticType) : GameUiEffect
    data class ShowToast(val message: String) : GameUiEffect
    data class GameOverNav(val winner: String) : GameUiEffect
}

enum class SoundType {
    CARD_PLAY,
    CARD_DRAW,
    MY_TURN,
    ULTIMO_SHOUT,
    VICTORY,
    DEFEAT,
    ERROR,
    SHIELD_DEFLECT,
    STACK_ACCUMULATE
}

enum class HapticType {
    LIGHT_TICK,
    MEDIUM_SNAP,
    HEAVY_WARNING
}
