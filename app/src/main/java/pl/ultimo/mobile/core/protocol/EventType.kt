package pl.ultimo.mobile.core.protocol

/**
 * Events and notifications emitted by the authoritative server to connected mobile clients.
 */
enum class EventType {
    WELCOME,
    ROOM_STATUS,
    HAND_UPDATE,
    TOP_CARD,
    TURN_ANNOUNCED,
    OPPONENT_UPDATE,
    DRAW_RESULT,
    ERROR,
    CHAT_BROADCAST,
    GAME_OVER,
    SERVER_SHUTDOWN,
    PONG
}
