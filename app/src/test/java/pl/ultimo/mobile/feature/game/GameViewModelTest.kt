package pl.ultimo.mobile.feature.game

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import pl.ultimo.mobile.core.database.dao.CardStatDao
import pl.ultimo.mobile.core.datastore.UserPreferences
import pl.ultimo.mobile.core.datastore.UserPreferencesRepository
import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor
import pl.ultimo.mobile.core.model.CardValue
import pl.ultimo.mobile.core.network.FakeWebSocketEngine
import pl.ultimo.mobile.core.protocol.ClientCommand
import pl.ultimo.mobile.core.protocol.ServerEvent
import pl.ultimo.mobile.domain.usecase.DeclareUltimoUseCase
import pl.ultimo.mobile.domain.usecase.DrawCardUseCase
import pl.ultimo.mobile.domain.usecase.PassTurnUseCase
import pl.ultimo.mobile.domain.usecase.PlayCardUseCase
import pl.ultimo.mobile.domain.usecase.RecordMatchOutcomeUseCase
import pl.ultimo.mobile.feature.game.model.GameUiIntent

/**
 * Coroutine stream and state-machine verification for [GameViewModel] using CashApp Turbine.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeWebSocketEngine = FakeWebSocketEngine()
    private val userPreferencesRepository = mockk<UserPreferencesRepository>()
    private val cardStatDao = mockk<CardStatDao>(relaxed = true)
    private val recordMatchOutcomeUseCase = mockk<RecordMatchOutcomeUseCase>(relaxed = true)

    private lateinit var playCardUseCase: PlayCardUseCase
    private lateinit var drawCardUseCase: DrawCardUseCase
    private lateinit var passTurnUseCase: PassTurnUseCase
    private lateinit var declareUltimoUseCase: DeclareUltimoUseCase
    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { userPreferencesRepository.userPreferences } returns flowOf(
            UserPreferences(savedPlayerHandle = "Hero")
        )

        playCardUseCase = PlayCardUseCase(fakeWebSocketEngine)
        drawCardUseCase = DrawCardUseCase(fakeWebSocketEngine)
        passTurnUseCase = PassTurnUseCase(fakeWebSocketEngine)
        declareUltimoUseCase = DeclareUltimoUseCase(fakeWebSocketEngine)

        val savedStateHandle = SavedStateHandle(mapOf("roomId" to "arena_99"))

        viewModel = GameViewModel(
            savedStateHandle = savedStateHandle,
            webSocketEngine = fakeWebSocketEngine,
            playCardUseCase = playCardUseCase,
            drawCardUseCase = drawCardUseCase,
            passTurnUseCase = passTurnUseCase,
            declareUltimoUseCase = declareUltimoUseCase,
            recordMatchOutcomeUseCase = recordMatchOutcomeUseCase,
            userPreferencesRepository = userPreferencesRepository,
            cardStatDao = cardStatDao
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialization_loadsSavedPlayerHandleAndRoomId() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.roomId).isEqualTo("arena_99")
            advanceUntilIdle()
            val updatedState = expectMostRecentItem()
            assertThat(updatedState.localPlayerName).isEqualTo("Hero")
        }
    }

    @Test
    fun serverEvent_handUpdate_updatesHandCardsAndUltimoState() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // initial

            // Simulate server providing initial hand of 2 cards
            val initialCards = listOf(
                Card(CardColor.RED, CardValue.SEVEN),
                Card(CardColor.BLUE, CardValue.THREE)
            )
            fakeWebSocketEngine.emitEvent(ServerEvent.HandUpdate(initialCards))
            advanceUntilIdle()

            val stateWithTwoCards = expectMostRecentItem()
            assertThat(stateWithTwoCards.hand).hasSize(2)
            assertThat(stateWithTwoCards.isUltimoCallable).isFalse()

            // Simulate hand reducing to 1 card
            val oneCard = listOf(Card(CardColor.RED, CardValue.SEVEN))
            fakeWebSocketEngine.emitEvent(ServerEvent.HandUpdate(oneCard))
            advanceUntilIdle()

            val stateWithOneCard = expectMostRecentItem()
            assertThat(stateWithOneCard.hand).hasSize(1)
            assertThat(stateWithOneCard.isUltimoCallable).isTrue()
        }
    }

    @Test
    fun serverEvent_topCard_updatesTableTopCardAndActiveColor() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem()

            val top = Card(CardColor.GREEN, CardValue.SKIP)
            fakeWebSocketEngine.emitEvent(ServerEvent.TopCard(card = top, activeColor = CardColor.GREEN))
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.topCard).isEqualTo(top)
            assertThat(state.activeColor).isEqualTo(CardColor.GREEN)
        }
    }

    @Test
    fun serverEvent_turnAnnounced_updatesMyTurnStatusAccurately() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem()
            advanceUntilIdle() // settle localPlayerName = "Hero"

            // 1. Opponent's turn announced
            fakeWebSocketEngine.emitEvent(ServerEvent.TurnAnnounced(activePlayerName = "Bob", timeLimitMs = 30000L))
            advanceUntilIdle()

            var state = expectMostRecentItem()
            assertThat(state.isMyTurn).isFalse()
            assertThat(state.activePlayerName).isEqualTo("Bob")

            // 2. Local player's turn announced
            fakeWebSocketEngine.emitEvent(ServerEvent.TurnAnnounced(activePlayerName = "Hero", timeLimitMs = 30000L))
            advanceUntilIdle()

            state = expectMostRecentItem()
            assertThat(state.isMyTurn).isTrue()
            assertThat(state.activePlayerName).isEqualTo("Hero")
        }
    }

    @Test
    fun intent_cardClicked_nonWild_dispatchesPlayCommandDirectly() = runTest(testDispatcher) {
        val cardToPlay = Card(CardColor.RED, CardValue.FIVE)

        viewModel.onIntent(GameUiIntent.CardClicked(cardToPlay))
        advanceUntilIdle()

        assertThat(fakeWebSocketEngine.sentCommands).hasSize(1)
        val command = fakeWebSocketEngine.sentCommands.first() as ClientCommand.Play
        assertThat(command.card).isEqualTo(cardToPlay)
        assertThat(command.chosenWildColor).isNull()
    }

    @Test
    fun intent_cardClicked_wild_opensWildColorPickerModal() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem()

            val wildCard = Card(CardColor.WILD, CardValue.WILD)
            viewModel.onIntent(GameUiIntent.CardClicked(wildCard))
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.isWildPickerVisible).isTrue()
            assertThat(state.pendingWildCard).isEqualTo(wildCard)
            // Verify no PLAY command dispatched yet
            assertThat(fakeWebSocketEngine.sentCommands).isEmpty()

            // Now player selects BLUE
            viewModel.onIntent(GameUiIntent.WildColorSelected(CardColor.BLUE))
            advanceUntilIdle()

            val stateAfterChoice = expectMostRecentItem()
            assertThat(stateAfterChoice.isWildPickerVisible).isFalse()
            assertThat(stateAfterChoice.pendingWildCard).isNull()

            assertThat(fakeWebSocketEngine.sentCommands).hasSize(1)
            val command = fakeWebSocketEngine.sentCommands.first() as ClientCommand.Play
            assertThat(command.card).isEqualTo(wildCard)
            assertThat(command.chosenWildColor).isEqualTo(CardColor.BLUE)
        }
    }

    @Test
    fun intent_drawCard_dispatchesDrawCommand() = runTest(testDispatcher) {
        viewModel.onIntent(GameUiIntent.DrawCard)
        advanceUntilIdle()

        assertThat(fakeWebSocketEngine.sentCommands).contains(ClientCommand.Draw)
    }

    @Test
    fun intent_passTurn_dispatchesPassCommand() = runTest(testDispatcher) {
        viewModel.onIntent(GameUiIntent.PassTurn)
        advanceUntilIdle()

        assertThat(fakeWebSocketEngine.sentCommands).contains(ClientCommand.Pass)
    }

    @Test
    fun intent_declareUltimo_sendsChatShoutCommand() = runTest(testDispatcher) {
        viewModel.onIntent(GameUiIntent.DeclareUltimo)
        advanceUntilIdle()

        val chatCommand = fakeWebSocketEngine.sentCommands.filterIsInstance<ClientCommand.Chat>().firstOrNull()
        assertThat(chatCommand).isNotNull()
        assertThat(chatCommand?.message).isEqualTo("ULTIMO!")
    }

    @Test
    fun serverEvent_gameOver_updatesWinnerState() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem()

            fakeWebSocketEngine.emitEvent(ServerEvent.GameOver(winnerName = "Hero", finalScores = emptyMap()))
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.isGameOver).isTrue()
            assertThat(state.gameOverWinner).isEqualTo("Hero")
        }
    }
}
