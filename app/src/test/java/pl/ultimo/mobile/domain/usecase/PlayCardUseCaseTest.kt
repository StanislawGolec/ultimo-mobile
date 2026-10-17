package pl.ultimo.mobile.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor
import pl.ultimo.mobile.core.model.CardValue
import pl.ultimo.mobile.core.network.WebSocketEngine
import pl.ultimo.mobile.core.protocol.ClientCommand

class PlayCardUseCaseTest {

    private val webSocketEngine: WebSocketEngine = mockk(relaxed = true)
    private lateinit var playCardUseCase: PlayCardUseCase

    @Before
    fun setup() {
        playCardUseCase = PlayCardUseCase(webSocketEngine)
        every { webSocketEngine.sendCommand(any()) } returns true
    }

    @Test
    fun playCard_matchingColor_succeedsAndDispatchesCommand() {
        val cardToPlay = Card(CardColor.RED, CardValue.FIVE)
        val topCard = Card(CardColor.RED, CardValue.TWO)

        val result = playCardUseCase(
            card = cardToPlay,
            topCard = topCard,
            activeColor = CardColor.RED
        )

        assertThat(result.isSuccess).isTrue()
        verify(exactly = 1) {
            webSocketEngine.sendCommand(ClientCommand.Play(cardToPlay, null))
        }
    }

    @Test
    fun playCard_matchingValueDifferentColor_succeeds() {
        val cardToPlay = Card(CardColor.BLUE, CardValue.FIVE)
        val topCard = Card(CardColor.RED, CardValue.FIVE)

        val result = playCardUseCase(
            card = cardToPlay,
            topCard = topCard,
            activeColor = CardColor.RED
        )

        assertThat(result.isSuccess).isTrue()
        verify(exactly = 1) {
            webSocketEngine.sendCommand(ClientCommand.Play(cardToPlay, null))
        }
    }

    @Test
    fun playCard_illegalMismatch_failsWithoutSendingCommand() {
        val cardToPlay = Card(CardColor.BLUE, CardValue.EIGHT)
        val topCard = Card(CardColor.RED, CardValue.FIVE)

        val result = playCardUseCase(
            card = cardToPlay,
            topCard = topCard,
            activeColor = CardColor.RED
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        verify(exactly = 0) {
            webSocketEngine.sendCommand(any())
        }
    }

    @Test
    fun playCard_wildWithoutColor_failsWithIllegalArgument() {
        val wildCard = Card(CardColor.WILD, CardValue.WILD)
        val topCard = Card(CardColor.RED, CardValue.FIVE)

        val result = playCardUseCase(
            card = wildCard,
            chosenWildColor = null,
            topCard = topCard,
            activeColor = CardColor.RED
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        verify(exactly = 0) {
            webSocketEngine.sendCommand(any())
        }
    }

    @Test
    fun playCard_wildWithColor_succeedsAndDispatchesCommand() {
        val wildCard = Card(CardColor.WILD, CardValue.WILD)
        val topCard = Card(CardColor.RED, CardValue.FIVE)

        val result = playCardUseCase(
            card = wildCard,
            chosenWildColor = CardColor.GREEN,
            topCard = topCard,
            activeColor = CardColor.RED
        )

        assertThat(result.isSuccess).isTrue()
        verify(exactly = 1) {
            webSocketEngine.sendCommand(ClientCommand.Play(wildCard, CardColor.GREEN))
        }
    }
}
