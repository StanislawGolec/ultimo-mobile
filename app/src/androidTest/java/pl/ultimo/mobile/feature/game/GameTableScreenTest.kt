package pl.ultimo.mobile.feature.game

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor
import pl.ultimo.mobile.core.model.CardValue
import pl.ultimo.mobile.feature.game.dialog.WildColorPickerDialog
import pl.ultimo.mobile.feature.game.model.OpponentUiModel

/**
 * Compose UI Semantics instrumentation tests verifying accessibility node hierarchy,
 * turn alerts, card clicks, and wild color modal interactions.
 */
@RunWith(AndroidJUnit4::class)
class GameTableScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gameTable_rendersRoomHeaderAndTurnIndicator() {
        composeTestRule.setContent {
            GameTableScreen(
                roomId = "championship_final",
                isMyTurn = true,
                activePlayerName = "Alice",
                topCard = Card(CardColor.RED, CardValue.SEVEN),
                activeColor = CardColor.RED
            )
        }

        // Verify Room ID displayed
        composeTestRule.onNodeWithText("Room: championship_final")
            .assertIsDisplayed()

        // Verify "YOUR TURN" status alert
        composeTestRule.onNodeWithText("YOUR TURN")
            .assertIsDisplayed()
    }

    @Test
    fun gameTable_cardClick_invokesPlayCallback() {
        var clickedCard: Card? = null
        val testHand = listOf(
            Card(CardColor.RED, CardValue.SEVEN),
            Card(CardColor.BLUE, CardValue.THREE)
        )

        composeTestRule.setContent {
            GameTableScreen(
                roomId = "room_1",
                hand = testHand,
                topCard = Card(CardColor.RED, CardValue.FIVE),
                activeColor = CardColor.RED,
                isMyTurn = true,
                onPlayCard = { card -> clickedCard = card }
            )
        }

        // Tap RED_7
        val cardNode = composeTestRule.onNodeWithContentDescription("Card RED_SEVEN")
        cardNode.assertIsDisplayed()
        cardNode.performClick()

        // A second tap on the selected card confirms play
        cardNode.performClick()

        // Callback must receive clickedCard == RED_7
        assert(clickedCard == Card(CardColor.RED, CardValue.SEVEN))
    }

    @Test
    fun gameTable_ultimoButton_disabledWithMultipleCards_enabledWithOneCard() {
        // 1. Hand of 2 cards -> ULTIMO disabled
        composeTestRule.setContent {
            GameTableScreen(
                hand = listOf(
                    Card(CardColor.RED, CardValue.SEVEN),
                    Card(CardColor.BLUE, CardValue.THREE)
                ),
                isUltimoCallable = false
            )
        }

        composeTestRule.onNodeWithText("ULTIMO!")
            .assertIsDisplayed()
            .assertIsNotEnabled()

        // 2. Hand with 1 card -> ULTIMO enabled
        composeTestRule.setContent {
            GameTableScreen(
                hand = listOf(Card(CardColor.RED, CardValue.SEVEN)),
                isUltimoCallable = true
            )
        }

        composeTestRule.onNodeWithText("ULTIMO!")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun wildColorPickerDialog_displaysFourColorsAndDispatchesChoice() {
        var chosenColor: CardColor? = null

        composeTestRule.setContent {
            WildColorPickerDialog(
                onColorSelected = { chosenColor = it }
            )
        }

        // Verify dialog header
        composeTestRule.onNodeWithText("CHOOSE COLOR")
            .assertIsDisplayed()

        // Click BLUE quadrant
        composeTestRule.onNodeWithText("BLUE")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()

        assert(chosenColor == CardColor.BLUE)
    }

    @Test
    fun gameTable_displaysOpponentWithUltimoWarning() {
        val opponents = listOf(
            OpponentUiModel(name = "Bob", cardCount = 1, isCurrentTurn = true)
        )

        composeTestRule.setContent {
            GameTableScreen(
                opponents = opponents
            )
        }

        // Verify opponent name
        composeTestRule.onNodeWithText("Bob")
            .assertIsDisplayed()

        // Verify "ULTIMO!" alert flag on opponent
        composeTestRule.onNodeWithText("ULTIMO!")
            .assertIsDisplayed()
    }
}
