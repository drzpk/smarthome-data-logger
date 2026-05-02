package dev.drzepka.smarthome.logger.core.pipeline.component.sender

import dev.drzepka.smarthome.common.util.Logger

internal class ConnectionErrorTracker(
    private val name: String,
    private val errorThreshold: Int,
    private val throttleSkipCount: Int,
    private val backoffFactor: Double,
    private val maxSkipCount: Int
) {
    private val log by Logger()

    var consecutiveErrors = 0
        private set

    private var throttled = false
    private var skipCountdown = 0
    private var currentSkipCount = throttleSkipCount

    fun shouldSkip(): Boolean {
        if (skipCountdown > 0) {
            skipCountdown--
            return true
        }

        if (throttled)
            skipCountdown = currentSkipCount

        return false
    }

    // Returns true if the error should be logged
    fun recordConnectionFailure(): Boolean {
        consecutiveErrors++
        if (consecutiveErrors == errorThreshold) {
            log.warn("{}: {} connection errors in a row, increasing send interval", name, errorThreshold)
            log.info("{}: subsequent connection errors won't be logged", name)
            throttled = true
            skipCountdown = currentSkipCount
            return false
        }
        if (consecutiveErrors > errorThreshold) {
            currentSkipCount = minOf((currentSkipCount * backoffFactor).toInt(), maxSkipCount)
            skipCountdown = currentSkipCount
            return false
        }
        return true
    }

    fun recordSuccess() {
        if (consecutiveErrors >= errorThreshold) {
            log.info("{}: connection restored after {} errors, resuming normal send interval", name, consecutiveErrors)
            throttled = false
            skipCountdown = 0
        }
        consecutiveErrors = 0
        currentSkipCount = throttleSkipCount
    }
}
