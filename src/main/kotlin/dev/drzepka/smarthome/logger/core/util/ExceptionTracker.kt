package dev.drzepka.smarthome.logger.core.util

/**
 * Tracks consecutive occurrences of the same exception chain.
 */
class ExceptionTracker {
    var exceptionCount = 0
        private set
    var exceptionChanged = false
        private set

    private var lastSignature: String? = null

    fun reset() {
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
            builder.appendln()

            currentException = currentException.cause
        }

        return builder.toString()
    }
}