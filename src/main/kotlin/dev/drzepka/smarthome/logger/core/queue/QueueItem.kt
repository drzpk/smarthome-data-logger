package dev.drzepka.smarthome.logger.core.queue

import java.time.Instant

data class QueueItem<T>(val content: T, val createdAt: Instant = Instant.now())