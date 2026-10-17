package pl.ultimo.mobile.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.ultimo.mobile.core.database.entity.CardStatEntity

@Dao
interface CardStatDao {

    @Query("SELECT * FROM card_stats ORDER BY timesPlayed DESC LIMIT :limit")
    fun observeMostPlayedCards(limit: Int = 10): Flow<List<CardStatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stat: CardStatEntity)

    @Query("SELECT * FROM card_stats WHERE cardToken = :cardToken LIMIT 1")
    suspend fun getCardStat(cardToken: String): CardStatEntity?

    @Query(
        """
        INSERT INTO card_stats (cardToken, timesPlayed, timesDrawn) 
        VALUES (:cardToken, 1, 0) 
        ON CONFLICT(cardToken) DO UPDATE SET timesPlayed = timesPlayed + 1
        """
    )
    suspend fun recordCardPlayed(cardToken: String)

    @Query(
        """
        INSERT INTO card_stats (cardToken, timesPlayed, timesDrawn) 
        VALUES (:cardToken, 0, 1) 
        ON CONFLICT(cardToken) DO UPDATE SET timesDrawn = timesDrawn + 1
        """
    )
    suspend fun recordCardDrawn(cardToken: String)
}
