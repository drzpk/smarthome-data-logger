package dev.drzepka.smarthome.logger.sensors.pipeline.decoder

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import dev.drzepka.smarthome.logger.sensors.model.LocalMeasurement
import dev.drzepka.smarthome.logger.sensors.model.MacAddress
import dev.drzepka.smarthome.logger.sensors.model.server.Measurement
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.ByteBuffer
import kotlin.experimental.and
import kotlin.experimental.xor
import kotlin.math.pow

/**
 * Datasheet: [link](https://dfimg.dfrobot.com/nobody/wiki/b37f8a2ff795acc7446c8defbb957054.PDF).
 */
object SHTC3Decoder : DataDecoder<Pair<MacAddress, ByteArray>, LocalMeasurement> {
    private val log by Logger()

    override fun decode(data: Pair<MacAddress, ByteArray>): List<LocalMeasurement> {
        val buffer = ByteBuffer.wrap(data.second)
        if (!checkCrc(data.second))
            return emptyList()

        val temperatureNumber = -45 + 175 * buffer.getShort(0) / 2.0.pow(16)
        var humidityNumber = 100 * buffer.getShort(3) / 2.0.pow(16)

        if (humidityNumber !in 0.0..100.0) {
            log.warn("Humidity value ({}) is outside the allowed range", humidityNumber)
            humidityNumber = humidityNumber.coerceIn(0.0..100.0)
        }

        // Set scale based on accuracy defined in the datasheet
        val temperature = BigDecimal.valueOf(temperatureNumber).setScale(1, RoundingMode.HALF_UP)
        val humidity = BigDecimal.valueOf(humidityNumber).setScale(0, RoundingMode.HALF_UP)

        val measurement = Measurement().apply {
            this.temperature = temperature
            this.humidity = humidity
        }

        val localmeasurement = LocalMeasurement(data.first, measurement)
        return listOf(localmeasurement)
    }

    private fun checkCrc(array: ByteArray): Boolean {
        var correct = true

        val temperature = array.sliceArray(0 until 2)
        if (!checkCrc(temperature, array[2])) {
            log.warn("Temperature CRC is incorrect. Value: {}, CRC: {}", temperature, array[2])
            correct = false
        }

        val humidity = array.sliceArray(3 until 5)
        if (!checkCrc(humidity, array[5])) {
            log.warn("Humidity CRC is incorrect. Value: {}, CRC: {}", humidity, array[5])
            correct = false
        }

        return correct
    }

    private fun checkCrc(values: ByteArray, expectedCrc: Byte): Boolean {
        val check = (0x80).toByte()
        val polynomial = (0x31).toByte()

        var crc = (0xff).toByte()
        for (value in values) {
            crc = crc xor value

            repeat(8) {
                val test = (crc and check).compareTo(0) != 0
                crc = (crc.toInt() and 0xff).shl(1).toByte()
                if (test)
                    crc = crc xor polynomial
            }
        }

        log.trace("Calculated CRC: {}", crc)
        return crc == expectedCrc
    }
}
