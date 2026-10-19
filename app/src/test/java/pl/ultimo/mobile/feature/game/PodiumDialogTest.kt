package pl.ultimo.mobile.feature.game

import androidx.compose.ui.test.assertIsDisplayed
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
import pl.ultimo.mobile.feature.game.components.DefenseHudBanner
import pl.ultimo.mobile.feature.game.dialog.PodiumDialog

/**
 * Jetpack Compose UI Semantics tests for [PodiumDialog] and [DefenseHudBanner].
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class PodiumDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun podiumDialog_rendersFirstSecondThirdAndSubmergedPlacements() {
        var returnedToLobby = false
        val standings = mapOf(
            1 to "Alice",
            2 to "Bob",
            3 to "Charlie",
            4 to "David"
        )

        composeTestRule.setContent {
            PodiumDialog(
                standings = standings,
                onReturnToLobby = { returnedToLobby = true }
            )
        }

        composeTestRule.onNodeWithTag("podium_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("podium_pedestal_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("podium_pedestal_2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("podium_pedestal_3").assertIsDisplayed()
        composeTestRule.onNodeWithTag("podium_pedestal_last").assertIsDisplayed()

        composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob").assertIsDisplayed()
        composeTestRule.onNodeWithText("Charlie").assertIsDisplayed()
        composeTestRule.onNodeWithText("SUBMERGED (#4): David").assertIsDisplayed()

        composeTestRule.onNodeWithTag("btn_return_to_lobby")
            .assertIsDisplayed()
            .performClick()

        assertThat(returnedToLobby).isTrue()
    }

    @Test
    fun defenseHudBanner_displaysSkipDeflectionAndPenaltyStackBanners() {
        var penaltyTaken = false

        composeTestRule.setContent {
            DefenseHudBanner(
                pendingSkipDeflection = true,
                pendingDrawPenalty = 4,
                autoPassNotice = "Auto-passed turn",
                onTakePenalty = { penaltyTaken = true }
            )
        }

        composeTestRule.onNodeWithTag("defense_skip_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("DEFEND: Play a SKIP card to deflect block to next player!")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("defense_penalty_banner")
            .assertIsDisplayed()
            .performClick()

        assertThat(penaltyTaken).isTrue()
        composeTestRule.onNodeWithTag("defense_auto_pass_banner").assertIsDisplayed()
    }
}
