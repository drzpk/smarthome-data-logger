package dev.drzepka.smarthome.logger.core.util

import dev.drzepka.smarthome.common.util.Logger

internal class ErrorTracker(
    private val name: String,
    private val errorThreshold: Int = Int.MAX_VALUE,
    private val throttleSkipCount: Int = 0,
    private val backoffFactor: Double = 1.0,
    private val maxSkipCount: Int = throttleSkipCount
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

    // Returns true if the error should be logged with full stacktrace
    fun recordFailure(): Boolean {
        consecutiveErrors++
        if (consecutiveErrors == errorThreshold) {
            log.warn("{}: {} errors in a row, throttling", name, errorThreshold)
            log.info("{}: subsequent errors will be logged without stacktrace", name)
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
            log.info("{}: recovered after {} errors", name, consecutiveErrors)
        }
        consecutiveErrors = 0
        throttled = false
        skipCountdown = 0
        currentSkipCount = throttleSkipCount
    }
}

internal suspend fun ErrorTracker.suspendRunCatching(
    log: org.slf4j.Logger,
    errorMessage: String,
    block: suspend () -> Unit
): Boolean {
    return try {
        block.invoke()
        recordSuccess()
        true
    } catch (e: Exception) {
        if (recordFailure())
            log.error("{}", errorMessage, e)
        else
            log.error("{}: {}", errorMessage, e.message)
        false
    }
}
