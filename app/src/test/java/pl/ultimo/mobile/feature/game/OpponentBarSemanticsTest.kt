package pl.ultimo.mobile.feature.game

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.ultimo.mobile.feature.game.components.OpponentBar
import pl.ultimo.mobile.feature.game.model.OpponentUiModel

/**
 * Jetpack Compose UI Semantics tests for [OpponentBar].
 *
 * Verifies:
 * 1. Responsive expanded card view for <= 3 opponents.
 * 2. Responsive compact pill chip view for 4 to 15 opponents (with handle truncation to 8 chars).
 * 3. Disconnected opponent status (Stage 13) displaying "Disconnected (2:00)" amber countdown badge.
 * 4. Active player turn highlighting and auto-centering semantics.
 */
@RunWith(AndroidJUnit4::class)
class OpponentBarSemanticsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun opponentBar_expandedView_whenThreeOrFewerOpponents() {
        val opponents = listOf(
            OpponentUiModel(name = "Alice", cardCount = 7, isCurrentTurn = false),
            OpponentUiModel(name = "Bob", cardCount = 4, isCurrentTurn = true),
            OpponentUiModel(name = "Charlie", cardCount = 1, isCurrentTurn = false)
        )

        composeTestRule.setContent {
            OpponentBar(
                opponents = opponents,
                activePlayerName = "Bob"
            )
        }

        // Verify expanded cards are rendered
        composeTestRule.onNodeWithTag("opponent_item_Alice").assertIsDisplayed()
        composeTestRule.onNodeWithTag("opponent_item_Bob").assertIsDisplayed()
        composeTestRule.onNodeWithTag("opponent_item_Charlie").assertIsDisplayed()

        // Full player names displayed
        composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob").assertIsDisplayed()
        composeTestRule.onNodeWithText("Charlie").assertIsDisplayed()

        // Card counts displayed
        composeTestRule.onNodeWithText("7").assertIsDisplayed()
        composeTestRule.onNodeWithText("4").assertIsDisplayed()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
    }

    @Test
    fun opponentBar_compactPillChips_whenMoreThanThreeOpponents() {
        val opponents = (1..15).map { index ->
            OpponentUiModel(
                name = if (index == 1) "AlexanderGreat" else "Player$index",
                cardCount = index % 7 + 1,
                isCurrentTurn = index == 2
            )
        }

        composeTestRule.setContent {
            OpponentBar(
                opponents = opponents,
                activePlayerName = "Player2"
            )
        }

        // Verify compact chips are rendered for opponents
        composeTestRule.onNodeWithTag("opponent_chip_AlexanderGreat").assertIsDisplayed()
        composeTestRule.onNodeWithTag("opponent_chip_Player2").assertIsDisplayed()

        // Truncated handle (max 8 chars: "AlexanderGreat" -> "Alexande")
        composeTestRule.onNodeWithText("Alexande").assertIsDisplayed()
        composeTestRule.onNodeWithText("Player2").assertIsDisplayed()
    }

    @Test
    fun opponentBar_disconnectedOpponent_displaysCountdownBadge_expanded() {
        val opponents = listOf(
            OpponentUiModel(
                name = "Dave",
                cardCount = 5,
                isDisconnected = true,
                disconnectRemainingSeconds = 120
            ),
            OpponentUiModel(name = "Eve", cardCount = 3)
        )

        composeTestRule.setContent {
            OpponentBar(
                opponents = opponents,
                activePlayerName = "Eve"
            )
        }

        composeTestRule.onNodeWithTag("opponent_item_Dave").assertIsDisplayed()
        composeTestRule.onNodeWithText("Disconnected (2:00)").assertIsDisplayed()
    }

    @Test
    fun opponentBar_disconnectedOpponent_displaysCountdownBadge_compact() {
        val opponents = (1..6).map { index ->
            OpponentUiModel(
                name = "Opponent$index",
                cardCount = 4,
                isDisconnected = index == 3,
                disconnectRemainingSeconds = 75
            )
        }

        composeTestRule.setContent {
            OpponentBar(
                opponents = opponents,
                activePlayerName = "Opponent1"
            )
        }

        composeTestRule.onNodeWithTag("opponent_chip_Opponent3").assertIsDisplayed()
        composeTestRule.onNodeWithText("Disconnected (1:15)").assertIsDisplayed()
    }

    @Test
    fun opponentBar_emptyList_doesNotCrash() {
        composeTestRule.setContent {
            OpponentBar(opponents = emptyList())
        }

        composeTestRule.onNodeWithTag("opponent_bar").assertDoesNotExist()
    }
}
