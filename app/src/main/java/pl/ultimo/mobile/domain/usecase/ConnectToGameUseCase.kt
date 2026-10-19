package pl.ultimo.mobile.domain.usecase

import pl.ultimo.mobile.core.datastore.UserPreferencesRepository
import pl.ultimo.mobile.core.network.WebSocketEngine
import javax.inject.Inject

class ConnectToGameUseCase @Inject constructor(
    private val webSocketEngine: WebSocketEngine,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(url: String, username: String, roomId: String?, accessCode: String? = null) {
        userPreferencesRepository.updateSavedPlayerHandle(username)
        webSocketEngine.connect(url, username, roomId, accessCode)
    }
}
