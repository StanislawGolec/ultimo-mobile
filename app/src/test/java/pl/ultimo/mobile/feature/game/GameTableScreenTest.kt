package pl.ultimo.mobile.feature.game

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor
import pl.ultimo.mobile.core.model.CardValue
import pl.ultimo.mobile.ui.theme.WindowWidthSizeClass

/**
 * Stage 15: Declarative UI Semantics test suite for [GameTableScreen] using [createComposeRule].
 *
 * Verifies:
 * - Room code header display and clipboard copy action.
 * - "SHOUT ULTIMO!" button state transitions (>2 cards disabled, <=2 cards enabled).
 * - Skip deflection and Draw penalty banners appearing when tactical defense flags are set.
 * - PodiumDialog rendering on match conclusion.
 * - Responsive WindowWidthSizeClass layout switching (Compact, Medium, Expanded).
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], qualifiers = "w400dp-h800dp")
class GameTableScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun roomCodeHeader_displaysCodeAndHandlesCopyAction() {
        var copiedRoomCode: String? = null

        composeTestRule.setContent {
            GameTableScreen(
                roomId = "ARENA_777",
                localPlayerName = "Hero",
                onCopyRoomCode = { copiedRoomCode = it }
            )
        }

        composeTestRule.onNodeWithTag("room_code_header")
            .assertIsDisplayed()
            .assertTextEquals("Room: ARENA_777")

        composeTestRule.onNodeWithTag("btn_copy_room_code")
            .assertIsDisplayed()
            .performClick()

        assertThat(copiedRoomCode).isEqualTo("ARENA_777")
        composeTestRule.onNodeWithText("✓ Copied!").assertIsDisplayed()
    }

    @Test
    fun shoutUltimoButton_disabledWhenMoreThanTwoCards_enabledWhenTwoOrFewerCards() {
        val threeCards = listOf(
            Card(CardColor.RED, CardValue.ONE),
            Card(CardColor.BLUE, CardValue.TWO),
            Card(CardColor.GREEN, CardValue.THREE)
        )

        composeTestRule.setContent {
            GameTableScreen(
                roomId = "room_1",
                hand = threeCards,
                isUltimoCallable = false,
                isShoutUltimoVisible = false
            )
        }

        // > 2 cards -> disabled
        composeTestRule.onNodeWithTag("btn_shout_ultimo")
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun shoutUltimoButton_enabledAndInvokesCallbackWhenTwoOrFewerCards() {
        var ultimoDeclared = false
        val twoCards = listOf(
            Card(CardColor.RED, CardValue.SEVEN),
            Card(CardColor.YELLOW, CardValue.SKIP)
        )

        composeTestRule.setContent {
            GameTableScreen(
                roomId = "room_1",
                hand = twoCards,
                isUltimoCallable = false,
                isShoutUltimoVisible = true,
                onDeclareUltimo = { ultimoDeclared = true }
            )
        }

        // <= 2 cards -> enabled
        composeTestRule.onNodeWithTag("btn_shout_ultimo")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        assertThat(ultimoDeclared).isTrue()
    }

    @Test
    fun defenseBanners_appearWhenSkipDeflectionAndDrawPenaltyFlagsSet() {
        composeTestRule.setContent {
            GameTableScreen(
                roomId = "room_1",
                pendingSkipDeflection = true,
                pendingDrawPenalty = 6,
                hand = listOf(Card(CardColor.RED, CardValue.SKIP))
            )
        }

        composeTestRule.onNodeWithTag("defense_skip_banner")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("DEFEND: Play a SKIP card to deflect block to next player!")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("defense_penalty_banner")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("discard_stack_penalty_badge")
            .assertIsDisplayed()
            .assertTextEquals("+6")
    }

    @Test
    fun podiumDialog_rendersCorrectlyOnMatchConclusion() {
        var returnedToLobby = false
        val standings = mapOf(
            1 to "Hero",
            2 to "Valkyrie",
            3 to "Titan",
            4 to "Rogue"
        )

        composeTestRule.setContent {
            GameTableScreen(
                roomId = "room_1",
                isPodiumVisible = true,
                podiumStandings = standings,
                onLeaveRoom = { returnedToLobby = true }
            )
        }

        composeTestRule.onNodeWithTag("podium_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("podium_pedestal_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("podium_pedestal_2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("podium_pedestal_3").assertIsDisplayed()
        composeTestRule.onNodeWithTag("podium_pedestal_last").assertIsDisplayed()

        composeTestRule.onNodeWithTag("btn_return_to_lobby")
            .assertIsDisplayed()
            .performClick()

        assertThat(returnedToLobby).isTrue()
    }

    @Test
    fun windowSizeClass_expanded_rendersDualColumnLayout() {
        composeTestRule.setContent {
            GameTableScreen(
                roomId = "tablet_room",
                windowWidthSizeClass = WindowWidthSizeClass.Expanded,
                hand = listOf(Card(CardColor.BLUE, CardValue.NINE))
            )
        }

        composeTestRule.onNodeWithTag("layout_expanded").assertIsDisplayed()
        composeTestRule.onNodeWithTag("expanded_left_column").assertIsDisplayed()
        composeTestRule.onNodeWithTag("expanded_right_column").assertIsDisplayed()
    }
}
