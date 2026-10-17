package pl.ultimo.mobile.core.protocol

import pl.ultimo.mobile.core.model.Card
import pl.ultimo.mobile.core.model.CardColor
import pl.ultimo.mobile.core.model.CardValue

/**
 * Encodes and decodes line-based wire protocol messages between raw ASCII wire format
 * and strongly-typed domain commands and events.
 * Wire protocol lines are terminated with '\n'.
 */
object ProtocolCodec {

    /**
     * Serializes a ClientCommand into wire-format string terminated by '\n'.
     */
    fun encodeCommand(command: ClientCommand): String {
        return when (command) {
            is ClientCommand.Join -> {
                if (!command.roomId.isNullOrBlank()) {
                    "JOIN ${command.username} ${command.roomId}\n"
                } else {
                    "JOIN ${command.username}\n"
                }
            }
            is ClientCommand.Play -> {
                val cardsStr = command.cards.joinToString(",") { formatCard(it) }
                val hasWild = command.cards.any { it.isWild }
                if (hasWild && command.chosenWildColor != null) {
                    "PLAY $cardsStr ${command.chosenWildColor}\n"
                } else {
                    "PLAY $cardsStr\n"
                }
            }
            ClientCommand.Draw -> "DRAW\n"
            ClientCommand.Pass -> "PASS\n"
            is ClientCommand.Chat -> "CHAT ${command.message}\n"
            ClientCommand.Ping -> "PING\n"
            ClientCommand.Start -> "START\n"
        }
    }

    /**
     * Parses a raw line received from the server into a strongly-typed ServerEvent.
     *
     * @param rawLine line received from server (with or without trailing newline)
     * @return parsed ServerEvent
     * @throws ProtocolException if the line format is invalid or opcode is unknown
     */
    @Throws(ProtocolException::class)
    fun decodeEvent(rawLine: String?): ServerEvent {
        if (rawLine.isNullOrBlank()) {
            throw ProtocolException("Empty or null event line received")
        }

        val line = rawLine.trim()
        val parts = line.split("\\s+".toRegex())
        val eventName = parts[0].uppercase()

        try {
            return when (eventName) {
                "WELCOME" -> {
                    if (parts.size < 2) {
                        throw ProtocolException("Malformed WELCOME event: $line")
                    }
                    val id = parts[1]
                    val message = if (parts.size > 2) {
                        line.substring(line.indexOf(parts[2])).trim()
                    } else {
                        ""
                    }
                    ServerEvent.Welcome(id, message)
                }
                "ROOM_STATUS" -> {
                    if (parts.size < 4) {
                        throw ProtocolException("Malformed ROOM_STATUS event: $line")
                    }
                    val roomId = parts[1]
                    val current = parts[2].toInt()
                    val max = parts[3].toInt()
                    val players = if (parts.size > 4 && parts[4] != "-") {
                        parts[4].split(",")
                    } else {
                        emptyList()
                    }
                    ServerEvent.RoomStatus(roomId, current, max, players)
                }
                "HAND_UPDATE" -> {
                    val cards = mutableListOf<Card>()
                    for (i in 1 until parts.size) {
                        val token = parts[i]
                        if (token.contains(",")) {
                            token.split(",").filter { it.isNotBlank() }.forEach {
                                cards.add(parseCard(it.trim()))
                            }
                        } else {
                            cards.add(parseCard(token))
                        }
                    }
                    ServerEvent.HandUpdate(cards)
                }
                "TOP_CARD" -> {
                    if (parts.size < 3) {
                        throw ProtocolException("Malformed TOP_CARD event: $line")
                    }
                    val card = parseCard(parts[1])
                    val color = parseColor(parts[2])
                    ServerEvent.TopCard(card, color)
                }
                "TURN_ANNOUNCED" -> {
                    if (parts.size < 2) {
                        throw ProtocolException("Malformed TURN_ANNOUNCED event: $line")
                    }
                    val activePlayer = parts[1]
                    val timeLimit = if (parts.size > 2) parts[2].toLongOrNull() ?: 30000L else 30000L
                    ServerEvent.TurnAnnounced(activePlayer, timeLimit)
                }
                "OPPONENT_UPDATE" -> {
                    if (parts.size < 3) {
                        throw ProtocolException("Malformed OPPONENT_UPDATE event: $line")
                    }
                    ServerEvent.OpponentUpdate(parts[1], parts[2].toInt())
                }
                "DRAW_RESULT" -> {
                    if (parts.size < 2) {
                        throw ProtocolException("Malformed DRAW_RESULT event: $line")
                    }
                    val card = parseCard(parts[1])
                    val playable = if (parts.size > 2) parts[2].toBooleanStrictOrNull() ?: false else false
                    ServerEvent.DrawResult(card, playable)
                }
                "ERROR" -> {
                    val msg = if (parts.size > 1) {
                        line.substring(line.indexOf(' ') + 1).trim()
                    } else {
                        "Unknown error"
                    }
                    ServerEvent.ErrorEvent(msg)
                }
                "CHAT_BROADCAST" -> {
                    if (parts.size < 2) {
                        throw ProtocolException("Malformed CHAT_BROADCAST event: $line")
                    }
                    val sender = parts[1]
                    val msg = if (parts.size > 2) {
                        line.substring(line.indexOf(parts[2])).trim()
                    } else {
                        ""
                    }
                    ServerEvent.ChatBroadcast(sender, msg)
                }
                "GAME_OVER" -> {
                    if (parts.size < 2) {
                        throw ProtocolException("Malformed GAME_OVER event: $line")
                    }
                    val winner = parts[1]
                    val scores = if (parts.size > 2) {
                        line.substring(line.indexOf(parts[2])).trim()
                    } else {
                        ""
                    }
                    ServerEvent.GameOver(winner, scores)
                }
                "SERVER_SHUTDOWN" -> {
                    val drainSeconds = if (parts.size > 1) parts[1].toIntOrNull() ?: 5 else 5
                    ServerEvent.ServerShutdown(drainSeconds)
                }
                "PONG" -> ServerEvent.Pong
                else -> throw ProtocolException("Unknown server event: $eventName")
            }
        } catch (pe: ProtocolException) {
            throw pe
        } catch (e: Exception) {
            throw ProtocolException("Failed to decode event '$line': ${e.message}", e)
        }
    }

    /**
     * Formats a card instance into wire representation.
     */
    fun formatCard(card: Card): String {
        return when (card.value) {
            CardValue.WILD -> "WILD"
            CardValue.WILD_DRAW_FOUR -> "WILD_DRAW_FOUR"
            else -> "${card.color.name}_${card.value.name}"
        }
    }

    /**
     * Parses a card token from wire representation into a Card instance.
     */
    @Throws(ProtocolException::class)
    fun parseCard(token: String?): Card {
        if (token.isNullOrBlank()) {
            throw ProtocolException("Empty card token")
        }

        val normalized = token.trim().uppercase()

        if (normalized == "WILD" || normalized == "WILD_WILD" || normalized == "WILD_COLOR") {
            return Card(CardColor.WILD, CardValue.WILD)
        }
        if (normalized == "WILD_DRAW_FOUR" || normalized == "WILD_DRAW4" ||
            normalized == "WILD_FOUR" || normalized == "WILD_WILD_DRAW_FOUR") {
            return Card(CardColor.WILD, CardValue.WILD_DRAW_FOUR)
        }

        val separator = normalized.indexOf('_')
        if (separator <= 0 || separator >= normalized.length - 1) {
            throw ProtocolException("Invalid card format (expected COLOR_VALUE, got: $token)")
        }

        val colorPart = normalized.substring(0, separator)
        val valuePart = normalized.substring(separator + 1)

        val color = parseColor(colorPart)
        val value = parseValue(valuePart)

        return Card(color, value)
    }

    /**
     * Parses a card color string into CardColor enum.
     */
    @Throws(ProtocolException::class)
    fun parseColor(token: String): CardColor {
        return try {
            CardColor.valueOf(token.trim().uppercase())
        } catch (e: IllegalArgumentException) {
            throw ProtocolException("Unknown card color: $token")
        }
    }

    /**
     * Parses a card value string or numeric token into CardValue enum.
     */
    @Throws(ProtocolException::class)
    fun parseValue(token: String): CardValue {
        return when (token.trim().uppercase()) {
            "0", "ZERO" -> CardValue.ZERO
            "1", "ONE" -> CardValue.ONE
            "2", "TWO" -> CardValue.TWO
            "3", "THREE" -> CardValue.THREE
            "4", "FOUR" -> CardValue.FOUR
            "5", "FIVE" -> CardValue.FIVE
            "6", "SIX" -> CardValue.SIX
            "7", "SEVEN" -> CardValue.SEVEN
            "8", "EIGHT" -> CardValue.EIGHT
            "9", "NINE" -> CardValue.NINE
            "SKIP" -> CardValue.SKIP
            "REVERSE" -> CardValue.REVERSE
            "DRAW_TWO", "DRAW2", "DRAW_2" -> CardValue.DRAW_TWO
            "WILD" -> CardValue.WILD
            "WILD_DRAW_FOUR", "WILD_DRAW4", "WILD_FOUR" -> CardValue.WILD_DRAW_FOUR
            else -> throw ProtocolException("Unknown card value: $token")
        }
    }
}
