package pl.ultimo.mobile.domain.usecase

import pl.ultimo.mobile.core.network.WebSocketEngine
import pl.ultimo.mobile.core.protocol.ClientCommand
import javax.inject.Inject

class DrawCardUseCase @Inject constructor(
    private val webSocketEngine: WebSocketEngine
) {
    operator fun invoke(): Boolean {
        return webSocketEngine.sendCommand(ClientCommand.Draw)
    }
}
