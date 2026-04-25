package dev.drzepka.smarthome.logger.core.pipeline.component.sender

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.model.measurement.CreateMeasurementsRequest
import dev.drzepka.smarthome.logger.core.queue.QueueItem
import dev.drzepka.smarthome.logger.core.transport.ServerRequestExecutor

class ServerDataSender(private val executor: ServerRequestExecutor) : DataSender {
    private val log by Logger()

    override suspend fun send(items: Collection<QueueItem>) {
        log.debug("Sending {} measurements to server", items.size)

        val measurements = items.map { it.content }
        val request = CreateMeasurementsRequest(measurements)

        executor.sendMeasurements(request)
    }
}