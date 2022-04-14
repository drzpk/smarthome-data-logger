package dev.drzepka.smarthome.logger.sensors.pipeline.listener.bluetooth

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.pipeline.component.DataListener
import dev.drzepka.smarthome.logger.sensors.exception.BluetoothException
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.BluetoothData
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.BluetoothServiceData
import java.util.concurrent.atomic.AtomicBoolean

class BluetoothCtlBluetoothListener : DataListener<BluetoothServiceData>() {
    private val log by Logger()

    private val started = AtomicBoolean(false)
    private var worker: Worker? = null
    private var process: Process? = null
    private var inputReader: InputReader? = null

    override fun start() {
        log.info("Starting listening for broadcasts")

        synchronized(this) {
            if (started.get())
                throw IllegalStateException("Already started")

            started.set(true)
            startProcess()
            enableScan()
        }
    }

    override fun stop() {
        log.info("Stopping listening for broadcasts")

        synchronized(this) {
            if (!started.get())
                return

            started.set(false)
            disableScan()
            stopProcess()
        }
    }

    private fun startProcess() {
        val builder = ProcessBuilder("bluetoothctl")
            .redirectErrorStream(true)

        process = try {
            builder.start()
        } catch (e: Exception) {
            throw BluetoothException("Cannot start bluetoothctl. Is the bluez-utilities package installed?", e)
        }

        inputReader = InputReader(process!!.inputStream)
    }

    private fun enableScan() {
        // Wait for the utility to start
        Thread.sleep(COMMAND_RESPONSE_WAIT_DELAY)

        if (isScanEnabled())
            return

        log.info("Enabling scanning")
        val result = sendCommand("menu scan\ntransport le\nback\nscan on", delayMultiplier = 3)

        if (!result.contains("Discovery started")) {
            log.error("Unable to start scanning")
            log.error("Command response: $result")
            throw BluetoothException("Unable to start scanning")
        }

        worker = Worker()
        Thread(worker).start()
    }

    private fun isScanEnabled(): Boolean {
        val response = sendCommand("show")

        val match = SHOW_COMMAND_RESPONSE_REGEX.find(response)
        if (match == null) {
            log.error("No controller available. Is current user added to the 'bluetooth' group?")
            log.error("Command response: $response")
            throw BluetoothException("No controller available")
        }

        val controllerMac = match.groupValues[1]
        val enabled = match.groupValues[2] == "yes"

        log.info("Scan for controller $controllerMac is ${if (enabled) "enabled" else "disabled"}")
        return enabled
    }

    private fun disableScan() {
        sendCommand("scan off")
    }

    private fun stopProcess() {
        worker!!.alive.set(false)
        sendCommand("exit")
        forceStopProcess()
        worker = null
        process = null
        inputReader = null
    }

    private fun forceStopProcess() {
        if (!process!!.isAlive)
            return
        log.warn("Exit command was sent but process is still alive, destroying")
        process!!.destroy()

        repeat(10) {
            Thread.sleep(100)
            if (!process!!.isAlive)
                return
        }

        log.error("Process couldn't be destroyed")
    }

    private fun sendCommand(cmd: String, delayMultiplier: Int = 1): String {
        process!!.outputStream.write("$cmd\n".toByteArray())
        process!!.outputStream.flush()

        Thread.sleep(COMMAND_RESPONSE_WAIT_DELAY * delayMultiplier)
        return inputReader!!.readAll()
    }

    inner class Worker : Runnable {
        val alive = AtomicBoolean(true)
        private val outputAnalyzer = OutputAnalyzer()

        override fun run() {
            while (started.get() && alive.get()) {
                val tokens = inputReader!!.readTokens()
                outputAnalyzer.analyze(tokens)
                    .forEach { handleData(it) }

                Thread.sleep(WORKER_WAKE_INTERVAL)
            }
        }

        private fun handleData(data: BluetoothData) {
            try {
                if (data is BluetoothServiceData)
                    onDataReceived(data)
            } catch (e: Exception) {
                log.error("Error while handling ${data::class.java.simpleName}", e)
            }
        }
    }

    companion object {
        private const val COMMAND_RESPONSE_WAIT_DELAY = 400L
        private const val WORKER_WAKE_INTERVAL = 1000L

        private val SHOW_COMMAND_RESPONSE_REGEX =
            Regex("Controller ((?:[A-F0-9]{2}:){5}[A-F0-9]{2}).*Discovering:\\s(yes|no)", RegexOption.DOT_MATCHES_ALL)
    }
}