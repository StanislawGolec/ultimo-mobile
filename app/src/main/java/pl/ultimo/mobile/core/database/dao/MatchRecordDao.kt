package pl.ultimo.mobile.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.ultimo.mobile.core.database.entity.MatchRecordEntity

@Dao
interface MatchRecordDao {

    @Query("SELECT * FROM match_records ORDER BY timestampEpochMs DESC")
    fun observeRecentMatches(): Flow<List<MatchRecordEntity>>

    @Query("SELECT * FROM match_records ORDER BY timestampEpochMs DESC LIMIT :limit")
    fun observeRecentMatches(limit: Int): Flow<List<MatchRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchRecordEntity)

    @Query("SELECT COUNT(*) FROM match_records")
    suspend fun getMatchCount(): Int

    @Query("DELETE FROM match_records")
    suspend fun clearHistory()
}
