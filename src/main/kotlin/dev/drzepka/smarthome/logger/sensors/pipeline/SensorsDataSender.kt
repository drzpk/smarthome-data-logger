package dev.drzepka.smarthome.logger.sensors.pipeline

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.pipeline.component.DataSender
import dev.drzepka.smarthome.logger.core.queue.QueueItem
import dev.drzepka.smarthome.logger.sensors.core.SensorsRequestExecutor
import dev.drzepka.smarthome.logger.sensors.model.LocalMeasurement
import dev.drzepka.smarthome.logger.sensors.model.server.CreateMeasurementsRequest
import java.time.Duration
import java.time.Instant

class SensorsDataSender(private val executor: SensorsRequestExecutor) : DataSender<LocalMeasurement> {
    private val log by Logger()

    override suspend fun send(items: Collection<QueueItem<LocalMeasurement>>) {
        log.debug("Sending {} measurements to server", items.size)

        val measurements = items.map {
            val measurement = it.content.measurement
            // todo: offset may not be a good idea
            measurement.timestampOffsetMillis = Duration.between(it.createdAt, Instant.now()).toMillis()
            measurement
        }

        val request = CreateMeasurementsRequest().apply {
            this.measurements.addAll(measurements)
        }

        executor.sendMeasurements(request)
    }
}
