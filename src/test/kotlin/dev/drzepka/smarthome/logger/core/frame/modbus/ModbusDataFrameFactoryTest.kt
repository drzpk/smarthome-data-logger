package dev.drzepka.smarthome.logger.core.frame.modbus

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ModbusDataFrameFactoryTest {

    @Test
    fun `should return empty collection for empty register list`() {
        val frames = ModbusDataFrameFactory(emptyList()).createDataFrames()
        assertEquals(0, frames.size)
    }

    @Test
    fun `should create single frame for single register`() {
        val frames = ModbusDataFrameFactory(listOf(IntModbusRegister("",100, 2))).createDataFrames()

        assertEquals(1, frames.size)
        frames.first().also {
            assertEquals(100, it.startAddress)
            assertEquals(1, it.qty)
        }
    }

    @Test
    fun `should create single frame for contiguous registers`() {
        // reg 100 (2 bytes = 1 Modbus reg), reg 101 (4 bytes = 2 regs), reg 103 (2 bytes = 1 reg)
        // span: 100..103 = 4 Modbus registers
        val registers = listOf(
            IntModbusRegister("",103, 2),
            IntModbusRegister("",100, 2),
            IntModbusRegister("",101, 4)
        )
        val frames = ModbusDataFrameFactory(registers).createDataFrames()

        assertEquals(1, frames.size)
        frames.first().also {
            assertEquals(100, it.startAddress)
            assertEquals(4, it.qty)
        }
    }

    @Test
    fun `should keep registers with gaps in one frame`() {
        // gap between 100 and 110 is allowed — unknown addresses are just skipped in the response
        val registers = listOf(
            IntModbusRegister("",100, 2),
            IntModbusRegister("",110, 2)
        )
        val frames = ModbusDataFrameFactory(registers).createDataFrames().toList()

        assertEquals(1, frames.size)
        assertEquals(100, frames[0].startAddress)
        assertEquals(11, frames[0].qty)  // span: 110+1-100=11
    }

    @Test
    fun `should split when span exceeds max register limit`() {
        // span = 125+1-0 = 126 > 125 → must split
        val registers = listOf(
            IntModbusRegister("",0, 2),
            IntModbusRegister("",125, 2)
        )
        val frames = ModbusDataFrameFactory(registers).createDataFrames().toList()

        assertEquals(2, frames.size)
        assertEquals(0, frames[0].startAddress);   assertEquals(1, frames[0].qty)
        assertEquals(125, frames[1].startAddress); assertEquals(1, frames[1].qty)
    }

    @Test
    fun `should keep registers together when span is exactly at the limit`() {
        // span = 124+1-0 = 125 = MAX_REGISTERS → fits in one frame
        val registers = listOf(
            IntModbusRegister("",0, 2),
            IntModbusRegister("",124, 2)
        )
        val frames = ModbusDataFrameFactory(registers).createDataFrames()

        assertEquals(1, frames.size)
        frames.first().also {
            assertEquals(0, it.startAddress)
            assertEquals(125, it.qty)
        }
    }

    @Test
    fun `should keep registers with multiple gaps in one frame`() {
        // All within a small span — gaps are allowed
        val registers = listOf(
            IntModbusRegister("",10, 2),
            IntModbusRegister("",11, 2),
            IntModbusRegister("",20, 4),
            IntModbusRegister("",50, 2)
        )
        val frames = ModbusDataFrameFactory(registers).createDataFrames().toList()

        assertEquals(1, frames.size)
        assertEquals(10, frames[0].startAddress)
        assertEquals(41, frames[0].qty)  // span: 50+1-10=41
    }

    @Test
    fun `should split a block that exceeds the limit`() {
        // 126 registers starting at 0 → span=126 > 125
        // First frame: 0..124 (125 regs), second frame: 125 (1 reg)
        val registers = (0..125).map { IntModbusRegister("",it, 2) }
        val frames = ModbusDataFrameFactory(registers).createDataFrames().toList()

        assertEquals(2, frames.size)
        assertEquals(0, frames[0].startAddress);   assertEquals(125, frames[0].qty)
        assertEquals(125, frames[1].startAddress); assertEquals(1, frames[1].qty)
    }
}
