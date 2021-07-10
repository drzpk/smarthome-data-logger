package dev.drzepka.smarthome.logger.sensors.bluetooth

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.sensors.converter.LittleEndianHexConverter
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.BluetoothServiceData
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.MacAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class MockBluetoothFacade : BluetoothFacade {
    private val log by Logger()
    private val listeners = ArrayList<BroadcastListener>()
    private val executor = Executors.newSingleThreadScheduledExecutor()

    private val random = Random.Default
    private var started = false

    override fun startListening() {
        if (started)
            return
        started = true

        executor.scheduleAtFixedRate({ createRandomMeasurement() }, 0L, 15L, TimeUnit.SECONDS)
    }

    override fun stopListening() {
        if (!started)
            return
        started = false

        executor.shutdown()
    }

    override fun addBroadcastListener(listener: BroadcastListener) {
        listeners.add(listener)
    }

    private fun createRandomMeasurement() {
        log.info("Creating random measurement")
        val temperature = LittleEndianHexConverter.convertToInt16(random.nextInt(1000, 3000))
        val humidity = LittleEndianHexConverter.convertToUInt16(random.nextInt(100, 10000))
        val batteryVoltage = LittleEndianHexConverter.convertToUInt16(random.nextInt(100, 3000))
        val batteryLevel = LittleEndianHexConverter.convertToUInt8(random.nextInt(0, 100))

        val binaryData = "11 11 11 11 11 11 $temperature $humidity $batteryVoltage $batteryLevel"
        val data = BluetoothServiceData(MacAddress("mac"), binaryData)

        listeners.forEach { it.onDataReceived(data) }
    }

}