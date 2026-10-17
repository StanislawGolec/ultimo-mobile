package pl.ultimo.mobile.core.network

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.random.Random

class ReconnectionPolicyTest {

    @Test
    fun computeDelayMs_deterministicWithoutJitter_scalesExponentiallyAndCaps() {
        val policy = ReconnectionPolicy(
            initialDelayMs = 1000L,
            maxDelayMs = 30000L,
            multiplier = 2.0,
            jitterFraction = 0.0,
            maxAttempts = 10
        )

        assertThat(policy.computeDelayMs(1)).isEqualTo(1000L)
        assertThat(policy.computeDelayMs(2)).isEqualTo(2000L)
        assertThat(policy.computeDelayMs(3)).isEqualTo(4000L)
        assertThat(policy.computeDelayMs(4)).isEqualTo(8000L)
        assertThat(policy.computeDelayMs(5)).isEqualTo(16000L)
        assertThat(policy.computeDelayMs(6)).isEqualTo(30000L) // Capped at maxDelayMs
        assertThat(policy.computeDelayMs(7)).isEqualTo(30000L)
    }

    @Test
    fun computeDelayMs_withJitter_staysWithinExpectedBounds() {
        val policy = ReconnectionPolicy(
            initialDelayMs = 1000L,
            maxDelayMs = 30000L,
            multiplier = 2.0,
            jitterFraction = 0.2, // +/- 20%
            maxAttempts = 10
        )

        for (attempt in 1..8) {
            val delay = policy.computeDelayMs(attempt)
            val base = (1000L * (1 shl (attempt - 1))).coerceAtMost(30000L)
            val minBound = (base * 0.8).toLong()
            val maxBound = (base * 1.2).toLong()

            assertThat(delay).isAtLeast(minBound)
            assertThat(delay).isAtMost(maxBound)
        }
    }

    @Test
    fun canAttemptAgain_stopsAtMaxAttempts() {
        val policy = ReconnectionPolicy(maxAttempts = 5)

        assertThat(policy.canAttemptAgain(1)).isTrue()
        assertThat(policy.canAttemptAgain(4)).isTrue()
        assertThat(policy.canAttemptAgain(5)).isFalse()
        assertThat(policy.canAttemptAgain(6)).isFalse()
    }
}
