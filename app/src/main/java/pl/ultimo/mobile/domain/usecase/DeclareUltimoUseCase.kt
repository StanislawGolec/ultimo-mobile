package pl.ultimo.mobile.domain.usecase

import pl.ultimo.mobile.core.network.WebSocketEngine
import pl.ultimo.mobile.core.protocol.ClientCommand
import javax.inject.Inject

class DeclareUltimoUseCase @Inject constructor(
    private val webSocketEngine: WebSocketEngine
) {
    operator fun invoke(handSize: Int): Result<Unit> {
        if (handSize != 1) {
            return Result.failure(IllegalStateException("Ultimo can only be declared when holding exactly 1 card (current hand size: $handSize)"))
        }

        val success = webSocketEngine.sendCommand(ClientCommand.Chat("ULTIMO!"))
        return if (success) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("Failed to broadcast Ultimo declaration over network"))
        }
    }
}
