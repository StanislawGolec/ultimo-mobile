package pl.ultimo.mobile.core.protocol

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor
import pl.ultimo.mobile.core.model.CardValue

class ProtocolCodecTest {

    // --- Command Encoding Tests ---

    @Test
    fun encodeCommand_joinWithoutRoom_formatsCorrectly() {
        val command = ClientCommand.Join("Alice")
        val wire = ProtocolCodec.encodeCommand(command)
        assertThat(wire).isEqualTo("JOIN Alice\n")
    }

    @Test
    fun encodeCommand_joinWithRoom_formatsCorrectly() {
        val command = ClientCommand.Join("Bob", "room_42")
        val wire = ProtocolCodec.encodeCommand(command)
        assertThat(wire).isEqualTo("JOIN Bob room_42\n")
    }

    @Test
    fun encodeCommand_playStandardCard_formatsCorrectly() {
        val command = ClientCommand.Play(Card(CardColor.RED, CardValue.SEVEN))
        val wire = ProtocolCodec.encodeCommand(command)
        assertThat(wire).isEqualTo("PLAY RED_SEVEN\n")
    }

    @Test
    fun encodeCommand_playWildCardWithColor_formatsCorrectly() {
        val command = ClientCommand.Play(Card(CardColor.WILD, CardValue.WILD), CardColor.BLUE)
        val wire = ProtocolCodec.encodeCommand(command)
        assertThat(wire).isEqualTo("PLAY WILD BLUE\n")
    }

    @Test
    fun encodeCommand_playWildDrawFourWithColor_formatsCorrectly() {
        val command = ClientCommand.Play(Card(CardColor.WILD, CardValue.WILD_DRAW_FOUR), CardColor.YELLOW)
        val wire = ProtocolCodec.encodeCommand(command)
        assertThat(wire).isEqualTo("PLAY WILD_DRAW_FOUR YELLOW\n")
    }

    @Test
    fun encodeCommand_playMultipleCards_formatsCommaSeparated() {
        val cards = listOf(
            Card(CardColor.BLUE, CardValue.FIVE),
            Card(CardColor.RED, CardValue.FIVE)
        )
        val command = ClientCommand.Play(cards)
        val wire = ProtocolCodec.encodeCommand(command)
        assertThat(wire).isEqualTo("PLAY BLUE_FIVE,RED_FIVE\n")
    }

    @Test
    fun encodeCommand_drawPassPingStart_formatsCorrectly() {
        assertThat(ProtocolCodec.encodeCommand(ClientCommand.Draw)).isEqualTo("DRAW\n")
        assertThat(ProtocolCodec.encodeCommand(ClientCommand.Pass)).isEqualTo("PASS\n")
        assertThat(ProtocolCodec.encodeCommand(ClientCommand.Ping)).isEqualTo("PING\n")
        assertThat(ProtocolCodec.encodeCommand(ClientCommand.Start)).isEqualTo("START\n")
    }

    @Test
    fun encodeCommand_chat_formatsCorrectly() {
        val command = ClientCommand.Chat("Good game!")
        val wire = ProtocolCodec.encodeCommand(command)
        assertThat(wire).isEqualTo("CHAT Good game!\n")
    }

    // --- Event Decoding Tests ---

    @Test
    fun decodeEvent_welcome_decodesIdAndMessage() {
        val event = ProtocolCodec.decodeEvent("WELCOME sess_101 Welcome Alice to Ultimo server\n")
        assertThat(event).isInstanceOf(ServerEvent.Welcome::class.java)
        val welcome = event as ServerEvent.Welcome
        assertThat(welcome.assignedPlayerId).isEqualTo("sess_101")
        assertThat(welcome.message).isEqualTo("Welcome Alice to Ultimo server")
    }

    @Test
    fun decodeEvent_welcomeWithoutMessage_decodesCleanly() {
        val event = ProtocolCodec.decodeEvent("WELCOME sess_102")
        assertThat(event).isInstanceOf(ServerEvent.Welcome::class.java)
        val welcome = event as ServerEvent.Welcome
        assertThat(welcome.assignedPlayerId).isEqualTo("sess_102")
        assertThat(welcome.message).isEmpty()
    }

    @Test
    fun decodeEvent_roomStatusWithPlayers_decodesCorrectly() {
        val event = ProtocolCodec.decodeEvent("ROOM_STATUS room_9 2 4 Alice,Bob")
        assertThat(event).isInstanceOf(ServerEvent.RoomStatus::class.java)
        val roomStatus = event as ServerEvent.RoomStatus
        assertThat(roomStatus.roomId).isEqualTo("room_9")
        assertThat(roomStatus.currentPlayers).isEqualTo(2)
        assertThat(roomStatus.maxPlayers).isEqualTo(4)
        assertThat(roomStatus.playerNames).containsExactly("Alice", "Bob").inOrder()
    }

    @Test
    fun decodeEvent_roomStatusEmptyPlayersHyphen_decodesEmptyList() {
        val event = ProtocolCodec.decodeEvent("ROOM_STATUS room_1 0 4 -")
        val roomStatus = event as ServerEvent.RoomStatus
        assertThat(roomStatus.playerNames).isEmpty()
    }

    @Test
    fun decodeEvent_handUpdate_decodesCardsList() {
        val event = ProtocolCodec.decodeEvent("HAND_UPDATE RED_5 BLUE_SKIP WILD GREEN_DRAW_TWO")
        assertThat(event).isInstanceOf(ServerEvent.HandUpdate::class.java)
        val hand = event as ServerEvent.HandUpdate
        assertThat(hand.cards).containsExactly(
            Card(CardColor.RED, CardValue.FIVE),
            Card(CardColor.BLUE, CardValue.SKIP),
            Card(CardColor.WILD, CardValue.WILD),
            Card(CardColor.GREEN, CardValue.DRAW_TWO)
        ).inOrder()
    }

    @Test
    fun decodeEvent_handUpdateEmpty_decodesEmptyList() {
        val event = ProtocolCodec.decodeEvent("HAND_UPDATE")
        val hand = event as ServerEvent.HandUpdate
        assertThat(hand.cards).isEmpty()
    }

    @Test
    fun decodeEvent_topCard_decodesCardAndActiveColor() {
        val event = ProtocolCodec.decodeEvent("TOP_CARD WILD BLUE")
        assertThat(event).isInstanceOf(ServerEvent.TopCard::class.java)
        val top = event as ServerEvent.TopCard
        assertThat(top.card).isEqualTo(Card(CardColor.WILD, CardValue.WILD))
        assertThat(top.activeColor).isEqualTo(CardColor.BLUE)
    }

    @Test
    fun decodeEvent_turnAnnounced_decodesPlayerAndTimer() {
        val event = ProtocolCodec.decodeEvent("TURN_ANNOUNCED Alice 25000")
        assertThat(event).isInstanceOf(ServerEvent.TurnAnnounced::class.java)
        val turn = event as ServerEvent.TurnAnnounced
        assertThat(turn.activePlayerName).isEqualTo("Alice")
        assertThat(turn.timeLimitMs).isEqualTo(25000L)
    }

    @Test
    fun decodeEvent_opponentUpdate_decodesNameAndCount() {
        val event = ProtocolCodec.decodeEvent("OPPONENT_UPDATE Bot_Alpha 6")
        assertThat(event).isInstanceOf(ServerEvent.OpponentUpdate::class.java)
        val opp = event as ServerEvent.OpponentUpdate
        assertThat(opp.opponentName).isEqualTo("Bot_Alpha")
        assertThat(opp.cardCount).isEqualTo(6)
    }

    @Test
    fun decodeEvent_drawResult_decodesDrawnCard() {
        val event = ProtocolCodec.decodeEvent("DRAW_RESULT YELLOW_DRAW2 true")
        assertThat(event).isInstanceOf(ServerEvent.DrawResult::class.java)
        val draw = event as ServerEvent.DrawResult
        assertThat(draw.drawnCard).isEqualTo(Card(CardColor.YELLOW, CardValue.DRAW_TWO))
        assertThat(draw.isPlayable).isTrue()
    }

    @Test
    fun decodeEvent_error_decodesErrorMessage() {
        val event = ProtocolCodec.decodeEvent("ERROR INVALID_MOVE Card cannot be played on top card")
        assertThat(event).isInstanceOf(ServerEvent.ErrorEvent::class.java)
        val err = event as ServerEvent.ErrorEvent
        assertThat(err.errorMessage).isEqualTo("INVALID_MOVE Card cannot be played on top card")
    }

    @Test
    fun decodeEvent_chatBroadcast_decodesSenderAndMessage() {
        val event = ProtocolCodec.decodeEvent("CHAT_BROADCAST Charlie Hey everyone, good luck!")
        assertThat(event).isInstanceOf(ServerEvent.ChatBroadcast::class.java)
        val chat = event as ServerEvent.ChatBroadcast
        assertThat(chat.sender).isEqualTo("Charlie")
        assertThat(chat.message).isEqualTo("Hey everyone, good luck!")
    }

    @Test
    fun decodeEvent_gameOver_decodesWinner() {
        val event = ProtocolCodec.decodeEvent("GAME_OVER Alice")
        assertThat(event).isInstanceOf(ServerEvent.GameOver::class.java)
        val over = event as ServerEvent.GameOver
        assertThat(over.winnerName).isEqualTo("Alice")
    }

    @Test
    fun decodeEvent_serverShutdown_decodesTimeout() {
        val event = ProtocolCodec.decodeEvent("SERVER_SHUTDOWN 10")
        assertThat(event).isInstanceOf(ServerEvent.ServerShutdown::class.java)
        val shutdown = event as ServerEvent.ServerShutdown
        assertThat(shutdown.drainTimeoutSeconds).isEqualTo(10)
    }

    @Test
    fun decodeEvent_pong_decodesPong() {
        val event = ProtocolCodec.decodeEvent("PONG\n")
        assertThat(event).isEqualTo(ServerEvent.Pong)
    }

    // --- Edge Cases & Exception Handling Tests ---

    @Test
    fun decodeEvent_nullOrBlank_throwsProtocolException() {
        assertThrows(ProtocolException::class.java) { ProtocolCodec.decodeEvent(null) }
        assertThrows(ProtocolException::class.java) { ProtocolCodec.decodeEvent("") }
        assertThrows(ProtocolException::class.java) { ProtocolCodec.decodeEvent("   \n") }
    }

    @Test
    fun decodeEvent_unknownOpcode_throwsProtocolException() {
        val ex = assertThrows(ProtocolException::class.java) {
            ProtocolCodec.decodeEvent("FOOBAR 123")
        }
        assertThat(ex).hasMessageThat().contains("Unknown server event: FOOBAR")
    }

    @Test
    fun parseCard_numericAndNamedValues_resolveIdentically() {
        val numeric = ProtocolCodec.parseCard("RED_7")
        val named = ProtocolCodec.parseCard("RED_SEVEN")
        assertThat(numeric).isEqualTo(named)
        assertThat(numeric).isEqualTo(Card(CardColor.RED, CardValue.SEVEN))
    }

    @Test
    fun parseCard_wildSynonyms_resolveCorrectly() {
        assertThat(ProtocolCodec.parseCard("WILD")).isEqualTo(Card(CardColor.WILD, CardValue.WILD))
        assertThat(ProtocolCodec.parseCard("WILD_COLOR")).isEqualTo(Card(CardColor.WILD, CardValue.WILD))
        assertThat(ProtocolCodec.parseCard("WILD_DRAW4")).isEqualTo(Card(CardColor.WILD, CardValue.WILD_DRAW_FOUR))
        assertThat(ProtocolCodec.parseCard("WILD_FOUR")).isEqualTo(Card(CardColor.WILD, CardValue.WILD_DRAW_FOUR))
    }

    @Test
    fun parseCard_malformedToken_throwsProtocolException() {
        assertThrows(ProtocolException::class.java) { ProtocolCodec.parseCard("INVALID") }
        assertThrows(ProtocolException::class.java) { ProtocolCodec.parseCard("PURPLE_FIVE") }
        assertThrows(ProtocolException::class.java) { ProtocolCodec.parseCard("RED_FIFTY") }
    }
}
