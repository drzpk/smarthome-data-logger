package dev.drzepka.smarthome.logger.core.device

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.model.Device
import dev.drzepka.smarthome.logger.core.model.MacAddress
import dev.drzepka.smarthome.logger.core.transport.ServerRequestExecutor
import dev.drzepka.smarthome.logger.core.util.ExceptionTracker
import dev.drzepka.smarthome.logger.core.util.suspendRunCatching
import kotlinx.coroutines.delay
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class OnlineDeviceManager(
    private val executor: ServerRequestExecutor,
    private val scheduler: TaskScheduler
) : DeviceManager {
    private val log by Logger()
    private val devices = ConcurrentHashMap<MacAddress, Device>()

    private var tracker = ExceptionTracker("OnlineDeviceManager")
    private var initialized = false

    override suspend fun initialize() {
        if (initialized) return
        initializeDevices()
        initialized = true
    }

    override fun start() {
        if (!initialized)
            throw IllegalStateException("Data source wasn't initialized")

        log.info("Scheduling device refresh at interval {}", DEVICE_REFRESH_INTERVAL)
        scheduler.schedule(TASK_NAME, DEVICE_REFRESH_INTERVAL) {
            refreshDevices()
        }
    }

    override fun stop() {
        scheduler.cancel(TASK_NAME)
    }

    override fun getDeviceId(mac: MacAddress): Int? = devices[mac]?.id

    override fun getDevices(): Map<MacAddress, Device> = devices.toMap()

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
