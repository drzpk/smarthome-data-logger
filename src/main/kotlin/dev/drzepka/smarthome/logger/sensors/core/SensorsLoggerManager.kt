package dev.drzepka.smarthome.logger.sensors.core

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.common.util.Mockable
import dev.drzepka.smarthome.logger.core.executor.ConnectionException
import dev.drzepka.smarthome.logger.core.queue.LoggerQueue
import dev.drzepka.smarthome.logger.core.queue.QueueBatch
import dev.drzepka.smarthome.logger.core.queue.QueueItem
import dev.drzepka.smarthome.logger.core.util.StopWatch
import dev.drzepka.smarthome.logger.sensors.converter.BluetoothServiceDataToMeasurementConverter
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.BluetoothServiceData
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.MacAddress
import dev.drzepka.smarthome.logger.sensors.model.server.CreateMeasurementsRequest
import dev.drzepka.smarthome.logger.sensors.model.server.Device
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Mockable
class SensorsLoggerManager(
    private val executor: SensorsRequestExecutor,
    private val queue: LoggerQueue<CreateMeasurementsRequest.Measurement>
) {

    private val log by Logger()
    private val devices = ConcurrentHashMap<MacAddress, Device>()

    suspend fun refreshDevices() {
        log.debug("Refreshing device list")

        val serverDevices = executor.getDevices()

        synchronized(devices) {
            devices.clear()
            serverDevices.forEach { devices[MacAddress(it.mac)] = it }
        }
    }

    fun decodeBluetoothData(bluetoothData: BluetoothServiceData) {
        val device = devices[bluetoothData.mac]
        if (device == null) {
            log.trace("Received bluetooth service data for unknown mac {}", bluetoothData.mac.value)
            return
        }

        val request = BluetoothServiceDataToMeasurementConverter.convertCustomFormat(bluetoothData)
        request.deviceId = device.id
        queue.enqueue(request)
    }

    suspend fun sendMeasurementsToServer(timeLimit: Duration) {
        val stopWatch = StopWatch(true)
        var timeLimitExceeded = false

        var iterationNo = 0
        while (queue.size() > 0 && !timeLimitExceeded) {

            if (++iterationNo > SEND_ITERATION_LIMIT) {
                log.warn("Iteration limit has been reached, force-stopping the loop")
                break
            }

            val batch = queue.getBatch()
            log.trace("Processing {} items starting at {}", batch.size, batch.items.firstOrNull()?.createdAt)

            processBatch(batch)
            if (stopWatch.current() > timeLimit) {
                stopWatch.stop()
                timeLimitExceeded = true

                log.warn(
                    "Some measurements ({}) couldn't be sent. Sending took {}, but time limit is {}",
                    queue.size(), stopWatch.elapsed(), timeLimit
                )
            }
        }
    }

    private suspend fun processBatch(batch: QueueBatch<CreateMeasurementsRequest.Measurement>) {
        val result = runCatching {
            sendMeasurements(batch.items)
        }

        // Don't remove batch because of connection failure.
        val exception = result.exceptionOrNull()
        if (exception is ConnectionException) {
            log.error("Cannot send {} measurements because of connection failure", batch.size)
        } else {
            queue.removeBatch(batch)
        }

        exception?.let { throw it }
    }

    private suspend fun sendMeasurements(items: Collection<QueueItem<CreateMeasurementsRequest.Measurement>>) {
        log.debug("Sending {} measurements to server", items.size)
        val measurements = items.map {
            val measurement = it.content
            measurement.timestampOffsetMillis = Duration.between(it.createdAt, Instant.now()).toMillis()
            measurement
        }

        val request = CreateMeasurementsRequest().apply {
            this.measurements.addAll(measurements)
        }
        executor.sendMeasurements(request)
    }

    companion object {
        private const val SEND_ITERATION_LIMIT = 30
    }
}
