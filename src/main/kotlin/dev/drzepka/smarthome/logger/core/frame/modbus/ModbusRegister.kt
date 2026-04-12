package dev.drzepka.smarthome.logger.core.frame.modbus

import java.math.BigDecimal
import kotlin.reflect.KClass

sealed class ModbusRegister<T : Any>(
    val name: String,
    val address: Int,
    val byteLength: Int,
    val valueProcessor: (T) -> T = { it }
) {
    abstract val dataType: KClass<T>
    val registerCount = (byteLength + 1) / 2

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModbusRegister<*>) return false
        return address == other.address && byteLength == other.byteLength
    }

    override fun hashCode(): Int = 31 * address + byteLength
}

class IntModbusRegister(
    name: String = "",
    address: Int,
    byteLength: Int,
    valueProcessor: (Int) -> Int = { it }
) : ModbusRegister<Int>(name, address, byteLength, valueProcessor) {
    override val dataType = Int::class

    init {
        require(byteLength in 1..Int.SIZE_BYTES) {
            "Int register byteLength must be 1..${Int.SIZE_BYTES}, got $byteLength"
        }
    }
}

class FloatModbusRegister(
    name: String = "",
    address: Int,
    byteLength: Int,
    valueProcessor: (Float) -> Float = { it }
) : ModbusRegister<Float>(name, address, byteLength, valueProcessor) {
    override val dataType = Float::class

    init {
        require(byteLength in 1..Float.SIZE_BYTES) {
            "Float register byteLength must be 1..${Float.SIZE_BYTES}, got $byteLength"
        }
    }
}

class BigDecimalModbusRegister(
    name: String = "",
    address: Int,
    byteLength: Int,
    valueProcessor: (BigDecimal) -> BigDecimal = { it }
) : ModbusRegister<BigDecimal>(name, address, byteLength, valueProcessor) {
    override val dataType = BigDecimal::class
}
