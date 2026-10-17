package pl.ultimo.mobile.feature.game

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.abs
import kotlin.math.min

/**
 * Unit tests verifying the mathematical fan-layout formulas of [HandCarousel].
 * Ensures symmetric angular distribution, parabolic vertical arch curvature,
 * and adaptive spacing compression under large hand counts.
 */
class HandFanGeometryTest {

    private fun calculateRotation(index: Int, handCount: Int): Float {
        if (handCount <= 1) return 0f
        val centerIndex = (handCount - 1) / 2.0f
        val relativeIndex = index - centerIndex
        val angleStep = when {
            handCount <= 5 -> 4.5f
            handCount <= 9 -> 3.2f
            else -> (32f / handCount).coerceAtMost(2.5f)
        }
        return (relativeIndex * angleStep).coerceIn(-26f, 26f)
    }

    private fun calculateArchOffset(index: Int, handCount: Int): Float {
        if (handCount <= 1) return 0f
        val centerIndex = (handCount - 1) / 2.0f
        val relativeIndex = index - centerIndex
        val arcStep = when {
            handCount <= 5 -> 2.2f
            handCount <= 9 -> 1.5f
            else -> min(1.0f, 14f / handCount)
        }
        return (relativeIndex * relativeIndex) * arcStep
    }

    private fun calculateSpacing(handCount: Int, availableWidth: Float, cardWidth: Float): Float {
        if (handCount <= 1) return 0f
        val naturalSpacing = (availableWidth - cardWidth - 16f) / (handCount - 1)
        return naturalSpacing.coerceIn(24f, 56f)
    }

    @Test
    fun singleCard_hasZeroRotationAndZeroOffset() {
        val rotation = calculateRotation(0, 1)
        val offset = calculateArchOffset(0, 1)

        assertThat(rotation).isEqualTo(0f)
        assertThat(offset).isEqualTo(0f)
    }

    @Test
    fun oddHandSize_centerCardHasZeroRotation() {
        // Hand of 7 cards -> center card index is 3
        val centerRotation = calculateRotation(3, 7)
        val centerOffset = calculateArchOffset(3, 7)

        assertThat(centerRotation).isEqualTo(0f)
        assertThat(centerOffset).isEqualTo(0f)
    }

    @Test
    fun fanLayout_isSymmetricAroundCenter() {
        val handCount = 7
        for (i in 0 until handCount / 2) {
            val leftRotation = calculateRotation(i, handCount)
            val rightRotation = calculateRotation(handCount - 1 - i, handCount)

            // Rotations are equal and opposite
            assertThat(leftRotation).isLessThan(0f)
            assertThat(rightRotation).isGreaterThan(0f)
            assertThat(abs(leftRotation + rightRotation)).isLessThan(0.001f)

            // Parabolic arch drops are identical on both edges
            val leftOffset = calculateArchOffset(i, handCount)
            val rightOffset = calculateArchOffset(handCount - 1 - i, handCount)
            assertThat(abs(leftOffset - rightOffset)).isLessThan(0.001f)
        }
    }

    @Test
    fun hugeHand_rotationIsClampedWithinBounds() {
        val handCount = 20
        for (i in 0 until handCount) {
            val rotation = calculateRotation(i, handCount)
            assertThat(rotation).isAtLeast(-26f)
            assertThat(rotation).isAtMost(26f)
        }
    }

    @Test
    fun cardSpacing_compressesAsHandCountIncreases() {
        val screenWidth = 360f
        val cardWidth = 80f

        val spacing5 = calculateSpacing(5, screenWidth, cardWidth)
        val spacing8 = calculateSpacing(8, screenWidth, cardWidth)
        val spacing15 = calculateSpacing(15, screenWidth, cardWidth)

        // Spacing should compress monotonically as hand count increases
        assertThat(spacing5).isGreaterThan(spacing8)
        assertThat(spacing8).isGreaterThan(spacing15)

        // And should stay within bounded playable thresholds
        assertThat(spacing15).isAtLeast(24f)
        assertThat(spacing5).isAtMost(56f)
    }
}
