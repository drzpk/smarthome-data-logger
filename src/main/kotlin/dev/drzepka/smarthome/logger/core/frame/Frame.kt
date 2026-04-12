package dev.drzepka.smarthome.logger.core.frame

interface Frame<T> {
    fun encodeRequest(): ByteArray
    fun decodeResponse(content: ByteArray): T?
}
