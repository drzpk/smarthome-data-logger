package dev.drzepka.smarthome.logger.core.queue

data class ProcessingResult(val `continue`: Boolean, val status: Status) {

    enum class Status {
        OK, SERVER_UNAVAILABLE
    }
}