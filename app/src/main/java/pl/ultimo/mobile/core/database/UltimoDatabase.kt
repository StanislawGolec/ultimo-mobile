package pl.ultimo.mobile.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import pl.ultimo.mobile.core.database.dao.CardStatDao
import pl.ultimo.mobile.core.database.dao.MatchRecordDao
import pl.ultimo.mobile.core.database.dao.PlayerDao
import pl.ultimo.mobile.core.database.entity.CardStatEntity
import pl.ultimo.mobile.core.database.entity.MatchRecordEntity
import pl.ultimo.mobile.core.database.entity.PlayerProfileEntity

/**
 * Main Room SQLite database providing persistence for player records, match logs, and card statistics.
 */
@Database(
    entities = [
        PlayerProfileEntity::class,
        MatchRecordEntity::class,
        CardStatEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UltimoDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao

    abstract fun matchRecordDao(): MatchRecordDao

    abstract fun cardStatDao(): CardStatDao

    companion object {
        const val DATABASE_NAME = "ultimo.db"
    }
}
