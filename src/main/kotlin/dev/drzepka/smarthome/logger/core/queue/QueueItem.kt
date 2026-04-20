package dev.drzepka.smarthome.logger.core.queue

import dev.drzepka.smarthome.logger.core.model.measurement.Measurement
import java.time.Instant

data class QueueItem(val content: Measurement, val createdAt: Instant = Instant.now())