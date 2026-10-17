package pl.ultimo.mobile.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.ultimo.mobile.core.database.UltimoDatabase
import pl.ultimo.mobile.core.database.dao.CardStatDao
import pl.ultimo.mobile.core.database.dao.MatchRecordDao
import pl.ultimo.mobile.core.database.dao.PlayerDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideUltimoDatabase(
        @ApplicationContext context: Context
    ): UltimoDatabase {
        return Room.databaseBuilder(
            context,
            UltimoDatabase::class.java,
            UltimoDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePlayerDao(database: UltimoDatabase): PlayerDao = database.playerDao()

    @Provides
    fun provideMatchRecordDao(database: UltimoDatabase): MatchRecordDao = database.matchRecordDao()

    @Provides
    fun provideCardStatDao(database: UltimoDatabase): CardStatDao = database.cardStatDao()
}
