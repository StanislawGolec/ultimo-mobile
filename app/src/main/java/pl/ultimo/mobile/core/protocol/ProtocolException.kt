package pl.ultimo.mobile.core.protocol

/**
 * Exception thrown when a protocol decoding error or malformed payload is encountered.
 */
class ProtocolException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
