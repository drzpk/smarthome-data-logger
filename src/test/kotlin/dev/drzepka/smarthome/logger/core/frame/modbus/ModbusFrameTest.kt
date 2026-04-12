package dev.drzepka.smarthome.logger.core.frame.modbus

import dev.drzepka.smarthome.logger.core.util.HexUtils
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ModbusFrameTest {

    @Test
    fun `should encode request frame`() {
        val dataFrame = ModbusDataFrame(1014, listOf(IntModbusRegister("", 1014, 4)))
        val frame = ModbusFrame.readInputRegisters(1, dataFrame)
        assertArrayEquals(HexUtils.hexToByteArray("01 04 03 f6 00 02 91 bd"), frame.encodeRequest())
    }

    @Test
    fun `should decode response`() {
        // response: slave=01, fn=04, byteCount=04, data=00 00 14 2B, crc=B4 9B
        // stripped data: 00 00 14 2B → 0x0000142B = 5163
        val register = IntModbusRegister("", 1014, 4)
        val dataFrame = ModbusDataFrame(1014, listOf(register))
        val frame = ModbusFrame.readInputRegisters(1, dataFrame)

        val result = frame.decodeResponse(HexUtils.hexToByteArray("01 04 04 00 00 14 2b b4 9b"))

        assertEquals(5163, result[register])
    }
}
