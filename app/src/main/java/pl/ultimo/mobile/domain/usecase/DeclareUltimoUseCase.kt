package pl.ultimo.mobile.domain.usecase

import pl.ultimo.mobile.core.network.WebSocketEngine
import pl.ultimo.mobile.core.protocol.ClientCommand
import javax.inject.Inject

class DeclareUltimoUseCase @Inject constructor(
    private val webSocketEngine: WebSocketEngine
) {
    operator fun invoke(handSize: Int): Result<Unit> {
        if (handSize !in 1..2) {
            return Result.failure(IllegalStateException("Ultimo can only be declared when holding 1 or 2 cards (current hand size: $handSize)"))
        }

        val success = webSocketEngine.sendCommand(ClientCommand.Chat("ULTIMO!"))
        return if (success) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("Failed to broadcast Ultimo declaration over network"))
        }
    }
}
