package pl.ultimo.mobile.core.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.ultimo.mobile.core.datastore.UserPreferencesRepository
import pl.ultimo.mobile.feature.game.model.HapticType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tactile feedback manager delivering nuanced micro-vibrations for card interactions,
 * turn alerts, and illegal move rejections.
 *
 * Utilizes [VibrationEffect] with amplitude modulation on Android O+ (API 26+)
 * with automatic fallback for older devices. Strictly respects user preference settings.
 */
@Singleton
class HapticFeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: UserPreferencesRepository
) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var isHapticsEnabled = true
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        coroutineScope.launch {
            preferencesRepository.userPreferences.collect { prefs ->
                isHapticsEnabled = prefs.isHapticsEnabled
            }
        }
    }

    /**
     * Triggers distinct tactile vibrations based on interaction severity.
     */
    fun performHaptic(type: HapticType) {
        if (!isHapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    HapticType.LIGHT_TICK -> {
                        // 12ms subtle micro-click (card selection, hover, turn arrival)
                        VibrationEffect.createOneShot(12L, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                    HapticType.MEDIUM_SNAP -> {
                        // 30ms crisp tactile snap (card drop into discard pile)
                        VibrationEffect.createOneShot(30L, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                    HapticType.HEAVY_WARNING -> {
                        // Double-pulse warning buzz (illegal move, turn timeout)
                        val timings = longArrayOf(0, 45, 60, 45)
                        val amplitudes = intArrayOf(0, 200, 0, 240)
                        VibrationEffect.createWaveform(timings, amplitudes, -1)
                    }
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                when (type) {
                    HapticType.LIGHT_TICK -> vibrator.vibrate(12L)
                    HapticType.MEDIUM_SNAP -> vibrator.vibrate(30L)
                    HapticType.HEAVY_WARNING -> vibrator.vibrate(longArrayOf(0, 45, 60, 45), -1)
                }
            }
        } catch (e: Exception) {
            // Haptic failure should never crash the UI
        }
    }
}
