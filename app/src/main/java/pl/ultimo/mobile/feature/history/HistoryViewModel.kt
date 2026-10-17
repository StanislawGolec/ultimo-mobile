package pl.ultimo.mobile.feature.history

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import pl.ultimo.mobile.core.database.dao.MatchRecordDao
import pl.ultimo.mobile.core.database.dao.PlayerDao
import pl.ultimo.mobile.core.database.entity.MatchRecordEntity
import pl.ultimo.mobile.core.database.entity.PlayerProfileEntity
import javax.inject.Inject

@Immutable
data class HistoryUiState(
    val profile: PlayerProfileEntity? = null,
    val recentMatches: List<MatchRecordEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    playerDao: PlayerDao,
    matchRecordDao: MatchRecordDao
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = combine(
        playerDao.observePlayerProfile(),
        matchRecordDao.observeRecentMatches(limit = 20)
    ) { profile, matches ->
        HistoryUiState(
            profile = profile ?: PlayerProfileEntity(),
            recentMatches = matches,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )
}
