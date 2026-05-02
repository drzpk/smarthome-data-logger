package dev.drzepka.smarthome.logger.core.pipeline.component.sender

import dev.drzepka.smarthome.logger.core.model.measurement.Measurement

interface DataSender {
    fun queue(items: Collection<Measurement>)
    suspend fun send(items: Collection<Measurement>)
}
