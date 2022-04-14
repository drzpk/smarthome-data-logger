package dev.drzepka.smarthome.logger.sensors.pipeline

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import dev.drzepka.smarthome.logger.sensors.core.DeviceManager
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.BluetoothServiceData
import dev.drzepka.smarthome.logger.sensors.model.server.Measurement
import java.math.BigDecimal
import java.math.RoundingMode

class BluetoothServiceDataDecoder(private val deviceManager: DeviceManager) :
    DataDecoder<BluetoothServiceData, Measurement> {

    private val log by Logger()

    /**
     * Decodes bluetooth data to measurement using
     * [custom format](https://github.com/pvvx/ATC_MiThermometer#custom-format-all-data-little-endian)
     */
    override fun decode(data: BluetoothServiceData): List<Measurement> {
        val deviceId = getDeviceId(data) ?: return emptyList()
        val bin = data.data

        val measurement = Measurement().apply {
            temperature = intLittleEndianToInt(
                bin,
                6,
                2
            ).toBigDecimal().divide(HUNDRED, 2, RoundingMode.UNNECESSARY)

            humidity = uintLittleEndianToInt(
                bin,
                8,
                2
            ).toBigDecimal().divide(HUNDRED, 2, RoundingMode.UNNECESSARY)

            batteryVoltage = uintLittleEndianToInt(
                bin,
                10,
                2
            ).toBigDecimal().divide(THOUSAND, 3, RoundingMode.UNNECESSARY)

            batteryLevel = bin[12].toInt()
            this.deviceId = deviceId
        }

        return listOf(measurement)
    }

    private fun getDeviceId(data: BluetoothServiceData): Int? {
        val id = deviceManager.getDeviceId(data.mac)
        if (id == null)
            log.trace("Received bluetooth service data for unknown mac {}", data.mac.value)

        return id
    }

    @Suppress("SameParameterValue")
    private fun intLittleEndianToInt(array: ByteArray, start: Int, size: Int): Int {
        var result = 0
        for (i in 0 until size) {
            val byte = array[start + i]
            val intval = if (i < size - 1) (byte.toInt() and 0xff) else byte.toInt()
            result += intval.shl(i * 8)
        }

        return result
    }

    @Suppress("SameParameterValue")
    private fun uintLittleEndianToInt(array: ByteArray, start: Int, size: Int): Int {
        var result = 0
        for (i in 0 until size) {
            val byte = array[start + i]
            result += (byte.toInt() and 0xff).shl(i * 8)
        }

        return result
    }

    companion object {
        private val HUNDRED = BigDecimal("100")
        private val THOUSAND = BigDecimal("1000")
    }
}
