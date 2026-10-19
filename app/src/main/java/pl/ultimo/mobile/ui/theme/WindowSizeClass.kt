package pl.ultimo.mobile.ui.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 WindowWidthSizeClass breakpoints for adaptive layout orchestration:
 * - Compact (< 600dp): Standard phones in portrait.
 * - Medium (600dp..840dp): Foldables (unfolded) and small tablets.
 * - Expanded (> 840dp): Large tablets (Pixel Tablet), desktop DeX, and ultrawide displays.
 */
@Immutable
enum class WindowWidthSizeClass {
    Compact,
    Medium,
    Expanded;

    companion object {
        fun fromWidthDp(widthDp: Int): WindowWidthSizeClass = when {
            widthDp < 600 -> Compact
            widthDp <= 840 -> Medium
            else -> Expanded
        }

        fun fromWidth(width: Dp): WindowWidthSizeClass = when {
            width < 600.dp -> Compact
            width <= 840.dp -> Medium
            else -> Expanded
        }
    }
}

/**
 * Immutable WindowSizeClass holder computed from the active screen configuration.
 */
@Immutable
data class WindowSizeClass(
    val widthSizeClass: WindowWidthSizeClass,
    val widthDp: Int = 400,
    val heightDp: Int = 800
)

/**
 * Calculates the [WindowSizeClass] for the given [activity] or current [LocalConfiguration].
 */
@Composable
fun calculateWindowSizeClass(activity: Activity? = null): WindowSizeClass {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    return remember(widthDp, heightDp) {
        WindowSizeClass(
            widthSizeClass = WindowWidthSizeClass.fromWidthDp(widthDp),
            widthDp = widthDp,
            heightDp = heightDp
        )
    }
}
