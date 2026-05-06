package dev.drzepka.smarthome.logger.core.frame.modbus

import dev.drzepka.smarthome.logger.core.frame.Frame
import java.math.BigDecimal

class ModbusDataFrame(
    val startAddress: Int,
    private val registers: List<ModbusRegister<*>>
) : Frame<ModbusRegisterData> {

    /** Number of 16-bit Modbus registers spanned by this frame (used as qty in ModbusFrame). */
    val qty: Int
        get() {
            if (registers.isEmpty()) return 0
            val last = registers.maxByOrNull { it.address }!!
            return last.address + (last.byteLength + 1) / 2 - startAddress
        }

    override fun encodeRequest(): ByteArray = byteArrayOf()

    override fun decodeResponse(content: ByteArray): ModbusRegisterData {
        val result = mutableMapOf<ModbusRegister<*>, Any>()
        for (register in registers) {
            val byteOffset = (register.address - startAddress) * 2
            result[register] = decodeRegister(register, content, byteOffset)
        }
        return result
    }

    private fun decodeRegister(register: ModbusRegister<*>, content: ByteArray, offset: Int): Any {
        var raw = 0L
        for (i in 0 until register.byteLength) {
            raw = (raw shl 8) or (content[offset + i].toLong() and 0xFF)
        }
        return when (register) {
            is IntModbusRegister -> register.valueProcessor(raw.toInt())
            is FloatModbusRegister -> register.valueProcessor(raw.toInt().toFloat())
            is BigDecimalModbusRegister -> register.valueProcessor(BigDecimal(raw))
        }
    }
}

typealias ModbusRegisterData = Map<ModbusRegister<*>, Any>

@Suppress("UNCHECKED_CAST")
fun <T : Any> ModbusRegisterData.getTyped(register: ModbusRegister<T>): T = this[register] as T