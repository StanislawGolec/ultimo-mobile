package pl.ultimo.mobile.domain.usecase

import pl.ultimo.mobile.core.database.dao.MatchRecordDao
import pl.ultimo.mobile.core.database.dao.PlayerDao
import pl.ultimo.mobile.core.database.entity.MatchRecordEntity
import pl.ultimo.mobile.core.database.entity.MatchResult
import java.util.UUID
import javax.inject.Inject

class RecordMatchOutcomeUseCase @Inject constructor(
    private val playerDao: PlayerDao,
    private val matchRecordDao: MatchRecordDao
) {
    suspend operator fun invoke(
        localPlayerName: String,
        winnerName: String,
        roomId: String,
        opponentCount: Int,
        cardsPlayed: Int,
        matchDurationSeconds: Long
    ) {
        val isVictory = localPlayerName.equals(winnerName, ignoreCase = true)
        val matchResult = if (isVictory) MatchResult.WIN else MatchResult.LOSS

        if (isVictory) {
            playerDao.recordVictory()
        } else {
            playerDao.recordDefeat()
        }

        val record = MatchRecordEntity(
            id = UUID.randomUUID().toString(),
            timestampEpochMs = System.currentTimeMillis(),
            roomId = roomId,
            opponentCount = opponentCount,
            result = matchResult,
            cardsPlayed = cardsPlayed,
            matchDurationSeconds = matchDurationSeconds
        )

        matchRecordDao.insertMatch(record)
    }
}
