package dev.drzepka.smarthome.logger.sensors.core

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.util.ExceptionTracker
import dev.drzepka.smarthome.logger.core.util.StopWatch
import dev.drzepka.smarthome.logger.sensors.bluetooth.BluetoothFacade
import dev.drzepka.smarthome.logger.sensors.bluetooth.BroadcastListener
import dev.drzepka.smarthome.logger.sensors.bluetooth.MockBluetoothFacade
import dev.drzepka.smarthome.logger.sensors.bluetooth.bluetoothctl.BluetoothCtlBluetoothFacade
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.BluetoothServiceData
import kotlinx.coroutines.delay
import java.time.Duration

class SensorsLoggerOrchestrator(
    private val manager: SensorsLoggerManager,
    private val scheduler: TaskScheduler,
    private val testMode: Boolean
) : BroadcastListener {

    private val log by Logger()

    private var bluetoothFacade: BluetoothFacade? = null

    private var deviceRefreshExceptionTracker = ExceptionTracker()
    private var measurementUploadExceptionTracker = ExceptionTracker()

    suspend fun start() {
        log.info("Starting logger orchestrator")
        initializeDevices()

        log.info("Launching bluetooth interface")
        createBluetoothFacade()
        bluetoothFacade!!.addBroadcastListener(this)
        bluetoothFacade!!.startListening()

        log.info("Scheduling device refresh at interval {}", DEVICE_REFRESH_INTERVAL)
        scheduler.schedule("sensors_deviceRefresh", DEVICE_REFRESH_INTERVAL) {
            executeDeviceRefresh()
        }

        log.info("Scheduling measurement uploading at interval {}", MEASUREMENT_SEND_INTERVAL)
        scheduler.schedule("sensors_measurementSend", MEASUREMENT_SEND_INTERVAL) {
            executeMeasurementUpload()
        }
    }

    fun stop() {
        log.info("Stopping logger orchestrator")
        bluetoothFacade?.stopListening()
    }

    override fun onDataReceived(data: BluetoothServiceData) {
        manager.decodeBluetoothData(data)
    }

    private suspend fun initializeDevices() {
        log.info("Initializing devices")

        var status: Boolean
        var trialNo = 1
        do {
            status = executeDeviceRefresh()
            if (!status) {
                log.info("Initializing unsuccessful, waiting {} before another trial", DEVICE_REFRESH_INTERVAL)
                delay(DEVICE_REFRESH_INTERVAL.toMillis())
                trialNo++
            }
        } while (!status)

        log.info("Devices initialized after {} trials", trialNo)
    }

    private suspend fun executeDeviceRefresh(): Boolean {
        return try {
            manager.refreshDevices()
            resetTracker(deviceRefreshExceptionTracker, "Refreshing devices")
            true
        } catch (e: Exception) {
            handleExceptionWithTracker(deviceRefreshExceptionTracker, e, "Error while refreshing devices")
            false
        }
    }

    private fun createBluetoothFacade() {
        bluetoothFacade = if (testMode) {
            log.info("Test is active, creating mock bluetooth facade")
            MockBluetoothFacade()
        } else {
            BluetoothCtlBluetoothFacade()
        }
    }

    private suspend fun executeMeasurementUpload() {
        try {
            val timeLimit = MEASUREMENT_SEND_INTERVAL.minusSeconds(10L)
            val stopWatch = StopWatch(true)
            manager.sendMeasurementsToServer(timeLimit)

            stopWatch.stop()
            if (stopWatch.elapsed() > timeLimit)
                log.warn("Measurement upload took longer than time limit ({} > {})", stopWatch.elapsed(), timeLimit)

            resetTracker(measurementUploadExceptionTracker, "Uploading measurements")
        } catch (e: Exception) {
            handleExceptionWithTracker(measurementUploadExceptionTracker, e, "Error while uploading measurements")
        }
    }

    private fun resetTracker(tracker: ExceptionTracker, info: String) {
        if (tracker.exceptionCount > 0)
            log.info("{} was successful after {} exceptions", info, tracker.exceptionCount)

        tracker.reset()
    }

    private fun handleExceptionWithTracker(tracker: ExceptionTracker, exception: Exception, errorMessage: String) {
        if (tracker.exceptionCount < CONSECUTIVE_EXCEPTION_THRESHOLD)
            log.error("{}", errorMessage, exception)
        else
            log.error("{}: {}", errorMessage, exception.message)

        tracker.setLastException(exception)

        if (tracker.exceptionCount == CONSECUTIVE_EXCEPTION_THRESHOLD)
            log.error("Upload error threshold has been reachced, consecutive identical errors will be presented without a stacktrace")
    }

    companion object {
        private val DEVICE_REFRESH_INTERVAL = Duration.ofSeconds(60)
        private val MEASUREMENT_SEND_INTERVAL = Duration.ofSeconds(30)
        private const val CONSECUTIVE_EXCEPTION_THRESHOLD = 3
    }
}