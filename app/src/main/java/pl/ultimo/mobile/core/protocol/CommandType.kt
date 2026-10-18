package pl.ultimo.mobile.core.protocol

/**
 * Commands initiated by the mobile client and sent to the authoritative game server.
 */
enum class CommandType {
    JOIN,
    PLAY,
    DRAW,
    PASS,
    CHAT,
    PING,
    START,
    ULTIMO,
    CATCH_ULTIMO,
    CHALLENGE_WILD_FOUR
}
