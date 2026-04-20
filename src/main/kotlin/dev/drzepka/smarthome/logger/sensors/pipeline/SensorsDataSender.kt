package dev.drzepka.smarthome.logger.sensors.pipeline

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.model.measurement.CreateMeasurementsRequest
import dev.drzepka.smarthome.logger.core.network.SensorsRequestExecutor
import dev.drzepka.smarthome.logger.core.pipeline.component.DataSender
import dev.drzepka.smarthome.logger.core.queue.QueueItem

class SensorsDataSender(private val executor: SensorsRequestExecutor) : DataSender {
    private val log by Logger()

    override suspend fun send(items: Collection<QueueItem>) {
        log.debug("Sending {} measurements to server", items.size)

        val measurements = items.map { it.content }
        val request = CreateMeasurementsRequest(measurements)

        executor.sendMeasurements(request)
    }
}
