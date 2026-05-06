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
    var exceptionChanged = false
        private set

    private var throttled = false
    private var skipCountdown = 0
    private var currentSkipCount = throttleSkipCount
    private var lastSignature: String? = null

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
    fun recordFailure(exception: Exception? = null): Boolean {
        if (exception != null)
            trackException(exception)

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
        exceptionChanged = false
        lastSignature = null
        throttled = false
        skipCountdown = 0
        currentSkipCount = throttleSkipCount
    }

    private fun trackException(exception: Exception) {
        val currentSignature = getExceptionSignature(exception)
        exceptionChanged = lastSignature != null && currentSignature != lastSignature
        lastSignature = currentSignature
    }

    private fun getExceptionSignature(exception: Exception): String {
        val builder = StringBuilder()
        var current: Throwable? = exception
        while (current != null) {
            builder.append(current.javaClass.canonicalName)
            builder.append('=')
            builder.append(current.message)
            builder.appendLine()
            current = current.cause
        }
        return builder.toString()
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
        if (recordFailure(e))
            log.error("{}", errorMessage, e)
        else
            log.error("{}: {}", errorMessage, e.message)
        false
    }
}
