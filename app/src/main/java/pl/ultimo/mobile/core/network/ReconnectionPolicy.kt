package pl.ultimo.mobile.core.network

import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Exponential backoff policy with randomized jitter to prevent thundering herd problems
 * during server reconnects or subway tunnel reconnection storms.
 *
 * Formula: delay = min(maxDelayMs, initialDelayMs * multiplier^attempt) +/- jitter
 */
data class ReconnectionPolicy(
    val initialDelayMs: Long = 1000L,
    val maxDelayMs: Long = 30000L,
    val multiplier: Double = 2.0,
    val jitterFraction: Double = 0.2,
    val maxAttempts: Int = 10,
    private val random: Random = Random.Default
) {
    /**
     * Computes the delay in milliseconds for a given attempt index (1-based).
     */
    fun computeDelayMs(attempt: Int): Long {
        if (attempt <= 0) return initialDelayMs

        val exponent = (attempt - 1).coerceAtLeast(0)
        val calculated = (initialDelayMs * multiplier.pow(exponent.toDouble())).toLong()
        val capped = min(maxDelayMs, calculated)

        if (jitterFraction <= 0.0) return capped

        val maxJitter = (capped * jitterFraction).toLong()
        val jitter = if (maxJitter > 0) random.nextLong(-maxJitter, maxJitter + 1) else 0L

        return (capped + jitter).coerceIn(100L, maxDelayMs + maxJitter)
    }

    /**
     * Determines whether further reconnect attempts should be made.
     */
    fun canAttemptAgain(attempt: Int): Boolean {
        return attempt < maxAttempts
    }
}
