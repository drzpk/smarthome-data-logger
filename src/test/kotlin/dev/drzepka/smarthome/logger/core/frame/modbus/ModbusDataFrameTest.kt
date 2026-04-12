package dev.drzepka.smarthome.logger.core.frame.modbus

import dev.drzepka.smarthome.logger.core.util.HexUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class ModbusDataFrameTest {

    @Test
    fun `should decode multiple registers from one response`() {
        // startAddress=0:
        //   reg 0 (2 bytes Int):   00 0A → 10
        //   reg 1 (2 bytes Int):   00 14 → 20
        //   reg 2 (2 bytes Float): 01 2C → raw 300 → 300.0f
        val regA = IntModbusRegister("",0, 2)
        val regB = IntModbusRegister("",1, 2)
        val regC = FloatModbusRegister("",2, 2)
        val frame = ModbusDataFrame(0, listOf(regA, regB, regC))
        val result = frame.decodeResponse(HexUtils.hexToByteArray("00 0A 00 14 01 2C"))

        assertEquals(10, result[regA])
        assertEquals(20, result[regB])
        assertEquals(300.0f, result[regC] as Float, 0.001f)
    }

    @Test
    fun `should calculate byte offset relative to start address`() {
        // startAddress=10, register at 12 → byteOffset=(12-10)*2=4
        // content: FF FF FF FF 00 2A → bytes at offset 4,5 = 00 2A → 42
        val register = IntModbusRegister("",12, 2)
        val frame = ModbusDataFrame(10, listOf(register))
        val result = frame.decodeResponse(HexUtils.hexToByteArray("FF FF FF FF 00 2A"))

        assertEquals(42, result[register])
    }

    @Nested
    inner class IntRegisters {

        @Test
        fun `should decode 2-byte value`() {
            // bytes 00 64 → 100
            val register = IntModbusRegister("",10, 2)
            val frame = ModbusDataFrame(10, listOf(register))
            val result = frame.decodeResponse(HexUtils.hexToByteArray("00 64"))

            assertEquals(100, result[register])
        }

        @Test
        fun `should decode 4-byte value spanning two Modbus registers`() {
            // bytes 00 01 86 A0 → 100000
            val register = IntModbusRegister("",10, 4)
            val frame = ModbusDataFrame(10, listOf(register))
            val result = frame.decodeResponse(HexUtils.hexToByteArray("00 01 86 A0"))

            assertEquals(100_000, result[register])
        }

        @Test
        fun `should decode 3-byte value with big-endian bit shifts`() {
            // bytes 01 02 03 → byte[0]<<16 | byte[1]<<8 | byte[2] = 0x010203 = 66051
            val register = IntModbusRegister("",100, 3)
            val frame = ModbusDataFrame(100, listOf(register))
            val result = frame.decodeResponse(HexUtils.hexToByteArray("01 02 03"))

            assertEquals(0x010203, result[register])
        }

        @Test
        fun `should apply valueProcessor`() {
            // raw 100 → processor (div 10) → 10
            val register = IntModbusRegister("",0, 2) { it / 10 }
            val frame = ModbusDataFrame(0, listOf(register))
            val result = frame.decodeResponse(HexUtils.hexToByteArray("00 64"))

            assertEquals(10, result[register])
        }
    }

    @Nested
    inner class FloatRegisters {

        @Test
        fun `should decode 2-byte value as float`() {
            // bytes 00 64 → raw int 100 → stored as 100.0f
            val register = FloatModbusRegister("",5, 2)
            val frame = ModbusDataFrame(5, listOf(register))
            val result = frame.decodeResponse(HexUtils.hexToByteArray("00 64"))

            assertEquals(100.0f, result[register] as Float, 0.001f)
        }

        @Test
        fun `should apply valueProcessor`() {
            // bytes 00 64 → raw int 100 → processor (* 0.1f) → 10.0f
            val register = FloatModbusRegister("",0, 2) { it * 0.1f }
            val frame = ModbusDataFrame(0, listOf(register))
            val result = frame.decodeResponse(HexUtils.hexToByteArray("00 64"))

            assertEquals(10.0f, result[register] as Float, 0.001f)
        }
    }

    @Nested
    inner class BigDecimalRegisters {

        @Test
        fun `should decode 2-byte value as BigDecimal`() {
            // bytes 00 64 → raw 100 → BigDecimal(100)
            val register = BigDecimalModbusRegister("", 0, 2)
            val frame = ModbusDataFrame(0, listOf(register))
            val result = frame.decodeResponse(HexUtils.hexToByteArray("00 64"))

            assertEquals(BigDecimal(100), result[register])
        }

        @Test
        fun `should apply valueProcessor`() {
            // bytes 03 E8 → raw 1000 → processor (* 0.1) → 100.0
            val register = BigDecimalModbusRegister("", 0, 2) { it.multiply(BigDecimal("0.1")) }
            val frame = ModbusDataFrame(0, listOf(register))
            val result = frame.decodeResponse(HexUtils.hexToByteArray("03 E8"))

            assertEquals(BigDecimal("100.0"), result[register])
        }
    }

    @Nested
    inner class Qty {

        @Test
        fun `should be 1 for single 2-byte register`() {
            assertEquals(1, ModbusDataFrame(10, listOf(IntModbusRegister("",10, 2))).qty)
        }

        @Test
        fun `should be 2 for single 4-byte register`() {
            assertEquals(2, ModbusDataFrame(10, listOf(IntModbusRegister("",10, 4))).qty)
        }

        @Test
        fun `should span from start to end of last register`() {
            // startAddress=10, last register at 15 (4 bytes = 2 regs) → qty = 15+2-10 = 7
            val registers = listOf(IntModbusRegister("",10, 2), IntModbusRegister("",15, 4))
            assertEquals(7, ModbusDataFrame(10, registers).qty)
        }

        @Test
        fun `should include gap between registers`() {
            // startAddress=100, last register at 110 (2 bytes = 1 reg) → qty = 110+1-100 = 11
            val registers = listOf(IntModbusRegister("",100, 2), IntModbusRegister("",110, 2))
            assertEquals(11, ModbusDataFrame(100, registers).qty)
        }
    }
}
