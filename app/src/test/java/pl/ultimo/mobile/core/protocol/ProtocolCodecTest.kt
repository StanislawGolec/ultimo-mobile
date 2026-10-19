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
    fun encodeCommand_joinWithAccessCodeAndMaxPlayers_formatsCorrectly() {
        val command = ClientCommand.Join("Charlie", "room_99", "PASS1", 16)
        val wire = ProtocolCodec.encodeCommand(command)
        assertThat(wire).isEqualTo("JOIN Charlie room_99 PASS1 16\n")

        val decoded = ProtocolCodec.decodeClientCommand(wire)
        assertThat(decoded).isEqualTo(command)
    }

    @Test
    fun encodeCommand_joinWithMaxPlayersOnly_formatsWithPlaceholder() {
        val command = ClientCommand.Join("Dave", "room_10", null, 8)
        val wire = ProtocolCodec.encodeCommand(command)
        assertThat(wire).isEqualTo("JOIN Dave room_10 - 8\n")

        val decoded = ProtocolCodec.decodeClientCommand(wire)
        assertThat(decoded).isEqualTo(command)
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

    @Test
    fun encodeCommand_tournamentCommands_formatCorrectly() {
        assertThat(ProtocolCodec.encodeCommand(ClientCommand.Ultimo)).isEqualTo("ULTIMO\n")
        assertThat(ProtocolCodec.encodeCommand(ClientCommand.CatchUltimo("Bob"))).isEqualTo("CATCH_ULTIMO Bob\n")
        assertThat(ProtocolCodec.encodeCommand(ClientCommand.ChallengeWildFour)).isEqualTo("CHALLENGE_WILD_FOUR\n")
    }

    @Test
    fun decodeClientCommand_tournamentCommands_decodeCleanly() {
        assertThat(ProtocolCodec.decodeClientCommand("ULTIMO\n")).isEqualTo(ClientCommand.Ultimo)

        val catchCmd = ProtocolCodec.decodeClientCommand("CATCH_ULTIMO Bob\n")
        assertThat(catchCmd).isEqualTo(ClientCommand.CatchUltimo("Bob"))

        assertThat(ProtocolCodec.decodeClientCommand("CHALLENGE_WILD_FOUR\n"))
            .isEqualTo(ClientCommand.ChallengeWildFour)
    }

    @Test
    fun decodeClientCommand_standardCommands_decodeCleanly() {
        assertThat(ProtocolCodec.decodeClientCommand("JOIN Alice room_1\n"))
            .isEqualTo(ClientCommand.Join("Alice", "room_1"))
        assertThat(ProtocolCodec.decodeClientCommand("DRAW\n")).isEqualTo(ClientCommand.Draw)
        assertThat(ProtocolCodec.decodeClientCommand("PASS\n")).isEqualTo(ClientCommand.Pass)
        assertThat(ProtocolCodec.decodeClientCommand("PING\n")).isEqualTo(ClientCommand.Ping)
        assertThat(ProtocolCodec.decodeClientCommand("START\n")).isEqualTo(ClientCommand.Start)
        assertThat(ProtocolCodec.decodeClientCommand("CHAT Hello all!\n"))
            .isEqualTo(ClientCommand.Chat("Hello all!"))
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

    @Test
    fun decodeEvent_ultimoDeclared_decodesPlayerName() {
        val event = ProtocolCodec.decodeEvent("ULTIMO_DECLARED Alice\n")
        assertThat(event).isInstanceOf(ServerEvent.UltimoDeclared::class.java)
        val ultimo = event as ServerEvent.UltimoDeclared
        assertThat(ultimo.playerName).isEqualTo("Alice")
    }

    @Test
    fun decodeEvent_ultimoPenalty_decodesDetails() {
        val event = ProtocolCodec.decodeEvent("ULTIMO_PENALTY Bob Alice 2\n")
        assertThat(event).isInstanceOf(ServerEvent.UltimoPenalty::class.java)
        val penalty = event as ServerEvent.UltimoPenalty
        assertThat(penalty.penalizedPlayer).isEqualTo("Bob")
        assertThat(penalty.challenger).isEqualTo("Alice")
        assertThat(penalty.penaltyCards).isEqualTo(2)
    }

    @Test
    fun decodeEvent_challengeResult_guilty_decodesCorrectly() {
        val event = ProtocolCodec.decodeEvent("CHALLENGE_RESULT Alice Bob GUILTY Bob 4\n")
        assertThat(event).isInstanceOf(ServerEvent.ChallengeResult::class.java)
        val challenge = event as ServerEvent.ChallengeResult
        assertThat(challenge.challenger).isEqualTo("Alice")
        assertThat(challenge.offender).isEqualTo("Bob")
        assertThat(challenge.isGuilty).isTrue()
        assertThat(challenge.penalizedPlayer).isEqualTo("Bob")
        assertThat(challenge.penaltyCards).isEqualTo(4)
    }

    @Test
    fun decodeEvent_challengeResult_innocent_decodesCorrectly() {
        val event = ProtocolCodec.decodeEvent("CHALLENGE_RESULT Alice Bob INNOCENT Alice 6\n")
        assertThat(event).isInstanceOf(ServerEvent.ChallengeResult::class.java)
        val challenge = event as ServerEvent.ChallengeResult
        assertThat(challenge.challenger).isEqualTo("Alice")
        assertThat(challenge.offender).isEqualTo("Bob")
        assertThat(challenge.isGuilty).isFalse()
        assertThat(challenge.penalizedPlayer).isEqualTo("Alice")
        assertThat(challenge.penaltyCards).isEqualTo(6)
    }

    @Test
    fun decodeEvent_roundOver_wonWithScores_decodesCorrectly() {
        val event = ProtocolCodec.decodeEvent("ROUND_OVER Alice 45 WON Alice:45,Bob:12\n")
        assertThat(event).isInstanceOf(ServerEvent.RoundOver::class.java)
        val roundOver = event as ServerEvent.RoundOver
        assertThat(roundOver.roundWinner).isEqualTo("Alice")
        assertThat(roundOver.pointsEarned).isEqualTo(45)
        assertThat(roundOver.isTournamentWon).isTrue()
        assertThat(roundOver.tournamentScores).containsExactly("Alice", 45, "Bob", 12)
    }

    @Test
    fun decodeEvent_roundOver_inProgressHyphenScores_decodesEmptyMap() {
        val event = ProtocolCodec.decodeEvent("ROUND_OVER Charlie 20 IN_PROGRESS -\n")
        assertThat(event).isInstanceOf(ServerEvent.RoundOver::class.java)
        val roundOver = event as ServerEvent.RoundOver
        assertThat(roundOver.roundWinner).isEqualTo("Charlie")
        assertThat(roundOver.pointsEarned).isEqualTo(20)
        assertThat(roundOver.isTournamentWon).isFalse()
        assertThat(roundOver.tournamentScores).isEmpty()
    }

    @Test
    fun encodeEvent_tournamentEvents_formatCorrectly() {
        val declared = ServerEvent.UltimoDeclared("Alice")
        assertThat(ProtocolCodec.encodeEvent(declared)).isEqualTo("ULTIMO_DECLARED Alice\n")

        val penalty = ServerEvent.UltimoPenalty("Bob", "Alice", 2)
        assertThat(ProtocolCodec.encodeEvent(penalty)).isEqualTo("ULTIMO_PENALTY Bob Alice 2\n")

        val challenge = ServerEvent.ChallengeResult("Alice", "Bob", true, "Bob", 4)
        assertThat(ProtocolCodec.encodeEvent(challenge)).isEqualTo("CHALLENGE_RESULT Alice Bob GUILTY Bob 4\n")

        val roundOverWon = ServerEvent.RoundOver("Alice", 45, true, mapOf("Alice" to 45, "Bob" to 12))
        assertThat(ProtocolCodec.encodeEvent(roundOverWon)).isEqualTo("ROUND_OVER Alice 45 WON Alice:45,Bob:12\n")

        val roundOverProgress = ServerEvent.RoundOver("Bob", 10, false, emptyMap())
        assertThat(ProtocolCodec.encodeEvent(roundOverProgress)).isEqualTo("ROUND_OVER Bob 10 IN_PROGRESS -\n")
    }

    // --- Edge Cases & Exception Handling Tests ---

    @Test
    fun decodeClientCommand_nullOrBlank_returnsNull() {
        assertThat(ProtocolCodec.decodeClientCommand(null)).isNull()
        assertThat(ProtocolCodec.decodeClientCommand("")).isNull()
        assertThat(ProtocolCodec.decodeClientCommand("   \n")).isNull()
    }

    @Test
    fun decodeClientCommand_unknownOpcode_throwsProtocolException() {
        val ex = assertThrows(ProtocolException::class.java) {
            ProtocolCodec.decodeClientCommand("INVALID_OPCODE arg\n")
        }
        assertThat(ex).hasMessageThat().contains("Unknown client command opcode: INVALID_OPCODE")
    }

    @Test
    fun decodeClientCommand_malformedCommands_throwsProtocolException() {
        assertThrows(ProtocolException::class.java) { ProtocolCodec.decodeClientCommand("JOIN") }
        assertThrows(ProtocolException::class.java) { ProtocolCodec.decodeClientCommand("PLAY") }
        assertThrows(ProtocolException::class.java) { ProtocolCodec.decodeClientCommand("CATCH_ULTIMO") }
    }

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
    fun decodeEvent_malformedTournamentEvents_throwsProtocolException() {
        assertThrows(ProtocolException::class.java) { ProtocolCodec.decodeEvent("ULTIMO_DECLARED") }
        assertThrows(ProtocolException::class.java) { ProtocolCodec.decodeEvent("ULTIMO_PENALTY Bob") }
        assertThrows(ProtocolException::class.java) { ProtocolCodec.decodeEvent("CHALLENGE_RESULT Alice Bob GUILTY") }
        assertThrows(ProtocolException::class.java) { ProtocolCodec.decodeEvent("ROUND_OVER Alice") }
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

    @Test
    fun encodeAndDecode_playerFinished() {
        val event = ServerEvent.PlayerFinished("Alice", 1)
        val encoded = ProtocolCodec.encodeEvent(event)
        assertThat(encoded).isEqualTo("PLAYER_FINISHED Alice 1\n")

        val decoded = ProtocolCodec.decodeEvent("PLAYER_FINISHED Alice 1")
        assertThat(decoded).isEqualTo(event)
    }

    @Test
    fun encodeAndDecode_matchStandings() {
        val standings = mapOf(1 to "Alice", 2 to "Bob", 3 to "Charlie", 4 to "David")
        val event = ServerEvent.MatchStandings(standings)
        val encoded = ProtocolCodec.encodeEvent(event)
        assertThat(encoded).isEqualTo("MATCH_STANDINGS 1:Alice 2:Bob 3:Charlie 4:David\n")

        val decoded = ProtocolCodec.decodeEvent("MATCH_STANDINGS 1:Alice 2:Bob 3:Charlie 4:David")
        assertThat(decoded).isEqualTo(event)
    }

    @Test
    fun decode_matchStandings_empty() {
        val decoded = ProtocolCodec.decodeEvent("MATCH_STANDINGS -")
        val standings = decoded as ServerEvent.MatchStandings
        assertThat(standings.placements).isEmpty()
    }
}
