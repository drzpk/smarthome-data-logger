package dev.drzepka.smarthome.logger.core.util

import dev.drzepka.smarthome.common.util.Logger

/**
 * Tracks consecutive occurrences of the same exception chain.
 */
class ExceptionTracker(val name: String) {
    private val log by Logger()

    var exceptionCount = 0
        private set
    var exceptionChanged = false
        private set

    private var lastSignature: String? = null

    fun reset() {
        if (exceptionCount > 0)
            log.info("{}: success after {} exceptions", name, exceptionCount)

        exceptionCount = 0
        exceptionChanged = false
        lastSignature = null
    }

    fun setLastException(exception: Exception) {
        val currentSignature = getExceptionSignature(exception)
        if (currentSignature == lastSignature) {
            exceptionCount++
            exceptionChanged = false
        } else {
            exceptionCount = 1
            exceptionChanged = lastSignature != null
        }

        lastSignature = currentSignature
    }

    private fun getExceptionSignature(exception: Exception): String {
        val builder = StringBuilder()

        var currentException: Throwable? = exception
        while (currentException != null) {
            builder.append(currentException.javaClass.canonicalName)
            builder.append('=')
            builder.append(currentException.message)
            builder.appendLine()

            currentException = currentException.cause
        }

        return builder.toString()
    }
}