package pl.ultimo.mobile.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import pl.ultimo.mobile.core.network.WebSocketEngine
import pl.ultimo.mobile.core.protocol.ClientCommand

class DeclareUltimoUseCaseTest {

    private val webSocketEngine: WebSocketEngine = mockk(relaxed = true)
    private lateinit var declareUltimoUseCase: DeclareUltimoUseCase

    @Before
    fun setup() {
        declareUltimoUseCase = DeclareUltimoUseCase(webSocketEngine)
        every { webSocketEngine.sendCommand(any()) } returns true
    }

    @Test
    fun declareUltimo_handSizeOne_succeeds() {
        val result = declareUltimoUseCase(handSize = 1)

        assertThat(result.isSuccess).isTrue()
        verify(exactly = 1) {
            webSocketEngine.sendCommand(ClientCommand.Chat("ULTIMO!"))
        }
    }

    @Test
    fun declareUltimo_handSizeGreaterThanOne_fails() {
        val result = declareUltimoUseCase(handSize = 4)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        verify(exactly = 0) {
            webSocketEngine.sendCommand(any())
        }
    }

    @Test
    fun declareUltimo_handSizeZero_fails() {
        val result = declareUltimoUseCase(handSize = 0)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        verify(exactly = 0) {
            webSocketEngine.sendCommand(any())
        }
    }
}
