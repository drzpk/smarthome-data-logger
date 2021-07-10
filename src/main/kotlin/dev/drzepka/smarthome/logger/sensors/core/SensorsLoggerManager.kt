package dev.drzepka.smarthome.logger.sensors.core

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.common.util.Mockable
import dev.drzepka.smarthome.logger.core.executor.ConnectionException
import dev.drzepka.smarthome.logger.core.executor.ResponseException
import dev.drzepka.smarthome.logger.sensors.converter.BluetoothServiceDataToMeasurementConverter
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.BluetoothServiceData
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.MacAddress
import dev.drzepka.smarthome.logger.sensors.model.server.CreateMeasurementsRequest
import dev.drzepka.smarthome.logger.sensors.model.server.Device
import dev.drzepka.smarthome.logger.core.queue.LoggerQueue
import dev.drzepka.smarthome.logger.core.util.StopWatch
import dev.drzepka.smarthome.logger.core.queue.ProcessingResult
import dev.drzepka.smarthome.logger.core.queue.QueueItem
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

    suspend fun sendMeasurementsToServer(timeLimit: Duration): Status {
        val stopWatch = StopWatch(true)

        var hasServerUnavailableError = false
        var timeLimitExceeded = false
        while (queue.size() > 0 && !timeLimitExceeded) {
            queue.processQueue {

                log.trace("Processing {} items starting at {}", it.size, it.firstOrNull()?.createdAt)
                val status = sendMeasurements(it)
                if (status == ProcessingResult.Status.SERVER_UNAVAILABLE)
                    hasServerUnavailableError = true

                val `continue` = if (stopWatch.current() > timeLimit) {
                    stopWatch.stop()
                    log.warn(
                        "Some measurements ({}) couldn't be sent. Sending took {}, but time limit is {}",
                        queue.size(), stopWatch.elapsed(), timeLimit
                    )
                    false
                } else {
                    true
                }

                timeLimitExceeded = !`continue`
                ProcessingResult(`continue` && !hasServerUnavailableError, status)
            }
        }

        return Status(hasServerUnavailableError)
    }

    private suspend fun sendMeasurements(items: Collection<QueueItem<CreateMeasurementsRequest.Measurement>>): ProcessingResult.Status {
        return try {
            doSendMeasurements(items)
        } catch (e: ConnectionException) {
            log.error("Connection exception", e)
            ProcessingResult.Status.SERVER_UNAVAILABLE
        } catch (e: ResponseException) {
            log.error(
                "Error while sending {} measurements starting at {}",
                items.size, items.firstOrNull()?.createdAt, e
            )
            ProcessingResult.Status.OK
        }
    }

    private suspend fun doSendMeasurements(items: Collection<QueueItem<CreateMeasurementsRequest.Measurement>>): ProcessingResult.Status {
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
        return ProcessingResult.Status.OK
    }

    data class Status(val serverUnavailable: Boolean)
}
