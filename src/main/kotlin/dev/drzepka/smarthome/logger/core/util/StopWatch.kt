package dev.drzepka.smarthome.logger.core.util

import java.time.Duration
import java.time.Instant

class StopWatch(start: Boolean = false) {
    private var startedAt: Instant? = null
    private var stoppedAt: Instant? = null

    init {
        if (start)
            start()
    }

    fun start() {
        startedAt = Instant.now()
    }

    fun stop() {
        stoppedAt = Instant.now()
    }

    fun current(): Duration {
        return Duration.between(startedAt, Instant.now())
    }

    fun elapsed(): Duration {
        return Duration.between(startedAt, stoppedAt)
    }
}