package pl.ultimo.mobile.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.ultimo.mobile.core.database.entity.PlayerProfileEntity

@Dao
interface PlayerDao {

    @Query("SELECT * FROM player_profiles WHERE id = :id LIMIT 1")
    fun observePlayerProfile(id: String = PlayerProfileEntity.DEFAULT_ID): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profiles WHERE id = :id LIMIT 1")
    suspend fun getPlayerProfile(id: String = PlayerProfileEntity.DEFAULT_ID): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: PlayerProfileEntity)

    @Query(
        """
        UPDATE player_profiles 
        SET totalMatches = totalMatches + 1, 
            totalWins = totalWins + 1, 
            currentStreak = currentStreak + 1, 
            bestStreak = CASE WHEN currentStreak + 1 > bestStreak THEN currentStreak + 1 ELSE bestStreak END 
        WHERE id = :id
        """
    )
    suspend fun recordVictory(id: String = PlayerProfileEntity.DEFAULT_ID)

    @Query(
        """
        UPDATE player_profiles 
        SET totalMatches = totalMatches + 1, 
            currentStreak = 0 
        WHERE id = :id
        """
    )
    suspend fun recordDefeat(id: String = PlayerProfileEntity.DEFAULT_ID)
}
