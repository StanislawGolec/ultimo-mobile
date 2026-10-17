package pl.ultimo.mobile.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Historical record of completed multiplayer game matches.
 */
@Entity(tableName = "match_records")
data class MatchRecordEntity(
    @PrimaryKey
    val id: String,
    val timestampEpochMs: Long,
    val roomId: String,
    val opponentCount: Int,
    val result: MatchResult,
    val cardsPlayed: Int,
    val matchDurationSeconds: Long,
    val finalScore: Int = 0
)

enum class MatchResult {
    WIN,
    LOSS,
    ABANDONED
}
