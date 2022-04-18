package dev.drzepka.smarthome.logger.sensors.core

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.common.util.Mockable
import dev.drzepka.smarthome.logger.core.pipeline.PipelineContext
import dev.drzepka.smarthome.logger.core.util.ExceptionTracker
import dev.drzepka.smarthome.logger.core.util.suspendRunCatching
import dev.drzepka.smarthome.logger.sensors.model.MacAddress
import dev.drzepka.smarthome.logger.sensors.model.server.Device
import kotlinx.coroutines.delay
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Mockable
class DeviceManager(private val executor: SensorsRequestExecutor) {
    private val log by Logger()
    private val devices = ConcurrentHashMap<MacAddress, Device>()

    private var tracker = ExceptionTracker("DeviceManager")
    private var initialized = false

    suspend fun initialize() {
        initializeDevices()
        initialized = true
    }

    fun start(context: PipelineContext) {
        if (!initialized)
            throw IllegalStateException("Data source wasn't initialized")

        log.info("Scheduling device refresh at interval {}", DEVICE_REFRESH_INTERVAL)
        context.scheduler.schedule(TASK_NAME, DEVICE_REFRESH_INTERVAL) {
            refreshDevices()
        }
    }

    fun stop(context: PipelineContext) {
        context.scheduler.cancel(TASK_NAME)
    }

    fun getDevices(): Map<MacAddress, Device> = devices.toMap()

    fun getDeviceId(mac: MacAddress): Int? = devices[mac]?.id

    private suspend fun initializeDevices() {
        log.info("Initializing devices")

        var status: Boolean
        var trialNo = 1
        do {
            status = refreshDevices()
            if (!status) {
                log.info("Initializing unsuccessful, waiting {} before another trial", DEVICE_REFRESH_INTERVAL)
                delay(DEVICE_REFRESH_INTERVAL.toMillis())
                trialNo++
            }
        } while (!status)

        log.info("Devices initialized after {} trials", trialNo)
    }

    private suspend fun refreshDevices(): Boolean {
        return tracker.suspendRunCatching(log, "Error while refreshing devices", 3) {
            doRefreshDevices()
        }
    }

    private suspend fun doRefreshDevices() {
        log.debug("Refreshing device list")

        val serverDevices = executor.getDevices()

        synchronized(devices) {
            devices.clear()
            serverDevices.forEach { devices[MacAddress(it.mac)] = it }
        }
    }

    companion object {
        private const val TASK_NAME = "sensors_deviceRefresh"
        private val DEVICE_REFRESH_INTERVAL = Duration.ofSeconds(60)
    }
}
