package dev.drzepka.smarthome.logger.sensors.converter

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

internal class LittleEndianHexConverterTest {

    @Test
    fun `should convert int16 to hex`() {
        then(LittleEndianHexConverter.convertToInt16(26_997)).isEqualTo("7569")
        then(LittleEndianHexConverter.convertToInt16(-22_442)).isEqualTo("56a8")
    }

    @Test
    fun `should convert uint16 to hex`() {
        then(LittleEndianHexConverter.convertToUInt16(26_997)).isEqualTo("7569")
        then(LittleEndianHexConverter.convertToUInt16(59_857)).isEqualTo("d1e9")
    }

    @Test
    fun `should convert uint8 to hex`() {
        then(LittleEndianHexConverter.convertToUInt8(78)).isEqualTo("4e")
        then(LittleEndianHexConverter.convertToUInt8(156)).isEqualTo("9c")
    }
}