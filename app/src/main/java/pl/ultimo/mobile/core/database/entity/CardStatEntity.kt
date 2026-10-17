package pl.ultimo.mobile.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Statistical tracking for card frequency, showing most played and drawn cards.
 */
@Entity(tableName = "card_stats")
data class CardStatEntity(
    @PrimaryKey
    val cardToken: String,
    val timesPlayed: Int = 0,
    val timesDrawn: Int = 0
)
