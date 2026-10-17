package pl.ultimo.mobile.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted local player profile entity caching game statistics and streak records.
 */
@Entity(tableName = "player_profiles")
data class PlayerProfileEntity(
    @PrimaryKey
    val id: String = DEFAULT_ID,
    val handle: String = "Player_1",
    val avatarIndex: Int = 0,
    val totalMatches: Int = 0,
    val totalWins: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0
) {
    val winRatePercent: Double
        get() = if (totalMatches > 0) (totalWins.toDouble() / totalMatches) * 100.0 else 0.0

    companion object {
        const val DEFAULT_ID = "local_player"
    }
}
