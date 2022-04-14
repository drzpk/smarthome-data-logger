package dev.drzepka.smarthome.logger.core.util

import org.slf4j.Logger

suspend fun ExceptionTracker.suspendRunCatching(
    log: Logger,
    errorMessage: String,
    exceptionThreshold: Int,
    block: suspend () -> Unit
): Boolean {
    return try {
        block.invoke()
        reset()

        true
    } catch (e: Exception) {
        if (exceptionCount < exceptionThreshold)
            log.error("{}", errorMessage, e)
        else
            log.error("{}: {}", errorMessage, e.message)

        setLastException(e)

        if (exceptionCount == exceptionThreshold)
            log.error("Upload error threshold has been reachced, consecutive identical errors will be presented without a stacktrace")

        false
    }
}