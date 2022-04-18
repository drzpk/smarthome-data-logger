package dev.drzepka.smarthome.logger.sensors.pipeline.decoder

import dev.drzepka.smarthome.logger.sensors.model.MacAddress
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class SHTC3DecoderTest {

    @Test
    fun `should decode temperature and humidity data`() {
        val mac = MacAddress("mac")
        val raw = intArrayOf(0x64, 0x73, 0xfc, 0x71, 0x8f, 0x1a).map { it.toByte() }.toByteArray()
        val decoded = SHTC3Decoder.decode(Pair(mac, raw))

        then(decoded).hasSize(1)
        then(decoded.first().measurement.temperature).isEqualTo(BigDecimal("23.7"))
        then(decoded.first().measurement.humidity).isEqualTo(BigDecimal("44"))
        then(decoded.first().mac).isSameAs(mac)
    }

    @Test
    fun `should not decode data when crc is invalid`() {
        val mac = MacAddress("mac")
        val raw = intArrayOf(0x64, 0x73, 0x12, 0x71, 0x8f, 0x34).map { it.toByte() }.toByteArray()
        val decoded = SHTC3Decoder.decode(Pair(mac, raw))

        then(decoded).isEmpty()
    }
}
