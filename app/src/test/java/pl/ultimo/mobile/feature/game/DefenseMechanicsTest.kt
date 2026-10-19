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
import pl.ultimo.mobile.feature.game.model.GameUiEffect
import pl.ultimo.mobile.feature.game.model.GameUiIntent
import pl.ultimo.mobile.feature.game.model.HapticType
import pl.ultimo.mobile.feature.game.model.SoundType

/**
 * Unit tests verifying Stage 11 Action Defending (Skip Deflection, +2/+4 Penalty Stacking,
 * Card Legality Filtering, Auto-Pass Feedback) and Stage 12 Lifecycle (Spectator Mode,
 * Match Standings Podium, and Catch Ultimo).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefenseMechanicsTest {

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

        viewModel = GameViewModel(
            savedStateHandle = SavedStateHandle(mapOf("roomId" to "tactical_room")),
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
    fun canPlayOn_whenPendingSkipDeflection_onlyAllowsSkipCards() {
        val topCard = Card(CardColor.RED, CardValue.SKIP)
        val blueSkip = Card(CardColor.BLUE, CardValue.SKIP)
        val redSeven = Card(CardColor.RED, CardValue.SEVEN)
        val wildCard = Card(CardColor.WILD, CardValue.WILD)

        assertThat(blueSkip.canPlayOn(topCard, CardColor.RED, pendingSkipDeflection = true)).isTrue()
        assertThat(redSeven.canPlayOn(topCard, CardColor.RED, pendingSkipDeflection = true)).isFalse()
        assertThat(wildCard.canPlayOn(topCard, CardColor.RED, pendingSkipDeflection = true)).isFalse()
    }

    @Test
    fun canPlayOn_whenPendingDrawPenalty_onlyAllowsMatchingDrawCards() {
        val topDrawTwo = Card(CardColor.GREEN, CardValue.DRAW_TWO)
        val topWildFour = Card(CardColor.WILD, CardValue.WILD_DRAW_FOUR)

        val redDrawTwo = Card(CardColor.RED, CardValue.DRAW_TWO)
        val wildDrawFour = Card(CardColor.WILD, CardValue.WILD_DRAW_FOUR)
        val greenFive = Card(CardColor.GREEN, CardValue.FIVE)

        // Against DRAW_TWO (+2 stack): DRAW_TWO or WILD_DRAW_FOUR can stack
        assertThat(redDrawTwo.canPlayOn(topDrawTwo, CardColor.GREEN, pendingDrawPenalty = 2)).isTrue()
        assertThat(wildDrawFour.canPlayOn(topDrawTwo, CardColor.GREEN, pendingDrawPenalty = 2)).isTrue()
        assertThat(greenFive.canPlayOn(topDrawTwo, CardColor.GREEN, pendingDrawPenalty = 2)).isFalse()

        // Against WILD_DRAW_FOUR (+4 stack): only WILD_DRAW_FOUR can stack
        assertThat(wildDrawFour.canPlayOn(topWildFour, CardColor.BLUE, pendingDrawPenalty = 4)).isTrue()
        assertThat(redDrawTwo.canPlayOn(topWildFour, CardColor.BLUE, pendingDrawPenalty = 4)).isFalse()
    }

    @Test
    fun playCardUseCase_rejectsNonDefendingCards_duringSkipDeflectionAndPenaltyStack() {
        val topSkip = Card(CardColor.YELLOW, CardValue.SKIP)
        val yellowNine = Card(CardColor.YELLOW, CardValue.NINE)
        val greenSkip = Card(CardColor.GREEN, CardValue.SKIP)

        // Non-skip rejected during deflection
        val failResult = playCardUseCase(
            card = yellowNine,
            topCard = topSkip,
            activeColor = CardColor.YELLOW,
            pendingSkipDeflection = true
        )
        assertThat(failResult.isFailure).isTrue()

        // Skip accepted during deflection
        val successResult = playCardUseCase(
            card = greenSkip,
            topCard = topSkip,
            activeColor = CardColor.YELLOW,
            pendingSkipDeflection = true
        )
        assertThat(successResult.isSuccess).isTrue()
    }

    @Test
    fun viewModel_skipDeflection_playsShieldDeflectSoundAndResetsFlag() = runTest(testDispatcher) {
        advanceUntilIdle()
        fakeWebSocketEngine.emitEvent(ServerEvent.TopCard(Card(CardColor.RED, CardValue.SKIP), CardColor.RED))
        fakeWebSocketEngine.emitEvent(ServerEvent.HandUpdate(listOf(Card(CardColor.BLUE, CardValue.SKIP))))
        advanceUntilIdle()

        viewModel.setDefenseState(pendingSkipDeflection = true)
        assertThat(viewModel.uiState.value.pendingSkipDeflection).isTrue()

        viewModel.uiEffect.test {
            viewModel.onIntent(GameUiIntent.CardClicked(Card(CardColor.BLUE, CardValue.SKIP)))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.pendingSkipDeflection).isFalse()
            val effect1 = awaitItem()
            val effect2 = awaitItem()
            assertThat(listOf(effect1, effect2)).contains(GameUiEffect.PlaySound(SoundType.SHIELD_DEFLECT))
            assertThat(listOf(effect1, effect2)).contains(GameUiEffect.TriggerHaptic(HapticType.MEDIUM_SNAP))
        }
    }

    @Test
    fun viewModel_penaltyStacking_drawCardTakesPenaltyWithHeavyWarningHaptic() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.setDefenseState(pendingDrawPenalty = 6)
        assertThat(viewModel.uiState.value.pendingDrawPenalty).isEqualTo(6)

        viewModel.uiEffect.test {
            viewModel.onIntent(GameUiIntent.DrawCard)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.pendingDrawPenalty).isEqualTo(0)
            val hapticEffect = awaitItem()
            assertThat(hapticEffect).isEqualTo(GameUiEffect.TriggerHaptic(HapticType.HEAVY_WARNING))
            assertThat(fakeWebSocketEngine.sentCommands).contains(ClientCommand.Draw)
        }
    }

    @Test
    fun viewModel_chatAutoPass_setsAndDismissesAutoPassNotice() = runTest(testDispatcher) {
        advanceUntilIdle()
        val autoPassMsg = "Hero drew a card but had no valid moves and auto-passed"
        fakeWebSocketEngine.emitEvent(ServerEvent.ChatBroadcast("GAME", autoPassMsg))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.autoPassNotice).isEqualTo(autoPassMsg)

        viewModel.onIntent(GameUiIntent.DismissAutoPass)
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.autoPassNotice).isNull()
    }

    @Test
    fun viewModel_playerFinishedAndMatchStandings_transitionsToSpectatorAndPodium() = runTest(testDispatcher) {
        advanceUntilIdle() // Hero loaded

        // Local player finishes 1st
        fakeWebSocketEngine.emitEvent(ServerEvent.PlayerFinished("Hero", 1))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isSpectating).isTrue()
        assertThat(viewModel.uiState.value.myPlacementRank).isEqualTo(1)

        // Match concludes with full standings
        val placements = mapOf(1 to "Hero", 2 to "Alice", 3 to "Bob", 4 to "Charlie")
        fakeWebSocketEngine.emitEvent(ServerEvent.MatchStandings(placements))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isPodiumVisible).isTrue()
        assertThat(viewModel.uiState.value.podiumStandings).isEqualTo(placements)
    }

    @Test
    fun viewModel_catchUltimo_sendsCatchUltimoCommand() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onIntent(GameUiIntent.CatchUltimo("Bob"))
        advanceUntilIdle()

        assertThat(fakeWebSocketEngine.sentCommands).contains(ClientCommand.CatchUltimo("Bob"))
    }
}
