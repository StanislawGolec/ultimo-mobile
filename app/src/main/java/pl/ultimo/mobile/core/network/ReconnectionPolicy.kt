package pl.ultimo.mobile.core.network

import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Exponential backoff policy with randomized jitter to prevent thundering herd problems
 * during server reconnects or subway tunnel reconnection storms.
 *
 * Formula: delay = min(maxDelayMs, initialDelayMs * multiplier^attempt) +/- jitter
 * Includes a 120-second (2-minute) disconnect grace period for active seat preservation.
 */
data class ReconnectionPolicy(
    val initialDelayMs: Long = 1000L,
    val maxDelayMs: Long = 30000L,
    val multiplier: Double = 2.0,
    val jitterFraction: Double = 0.2,
    val maxAttempts: Int = 10,
    val gracePeriodMs: Long = 120_000L,
    private val random: Random = Random.Default
) {
    /**
     * Secondary constructor supporting legacy naming parameters (factor, jitterFactor).
     */
    constructor(
        initialDelayMs: Long = 1000L,
        maxDelayMs: Long = 30000L,
        factor: Double = 2.0,
        jitterFactor: Double = 0.2,
        maxAttempts: Int = 10
    ) : this(
        initialDelayMs = initialDelayMs,
        maxDelayMs = maxDelayMs,
        multiplier = factor,
        jitterFraction = jitterFactor,
        maxAttempts = maxAttempts,
        gracePeriodMs = 120_000L,
        random = Random.Default
    )

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
     * Compatibility alias for computeDelayMs.
     */
    fun calculateDelayMs(attempt: Int): Long = computeDelayMs(attempt)

    /**
     * Determines whether further reconnect attempts should be made based on max attempts.
     */
    fun canAttemptAgain(attempt: Int): Boolean {
        return attempt < maxAttempts
    }

    /**
     * Checks whether the current elapsed disconnection time is within the 120s grace period.
     */
    fun isWithinGracePeriod(disconnectTimestampMs: Long): Boolean {
        if (disconnectTimestampMs <= 0L) return true
        val elapsed = System.currentTimeMillis() - disconnectTimestampMs
        return elapsed < gracePeriodMs
    }

    /**
     * Returns remaining grace period seconds (clamped at 0..120).
     */
    fun remainingGracePeriodSeconds(disconnectTimestampMs: Long): Int {
        if (disconnectTimestampMs <= 0L) return (gracePeriodMs / 1000).toInt()
        val elapsed = System.currentTimeMillis() - disconnectTimestampMs
        return ((gracePeriodMs - elapsed) / 1000).coerceAtLeast(0).toInt()
    }
}
