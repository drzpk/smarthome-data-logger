package dev.drzepka.smarthome.logger.pv.source.afore

import dev.drzepka.smarthome.logger.core.frame.modbus.BigDecimalModbusRegister
import dev.drzepka.smarthome.logger.core.frame.modbus.FloatModbusRegister
import dev.drzepka.smarthome.logger.core.frame.modbus.IntModbusRegister
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusRegister
import java.math.BigDecimal

object AforeT6Registers {

    val registers: List<ModbusRegister<*>> by lazy {
        listOf(
            gridVoltageA, gridCurrentA, gridFrequencyA, activePowerA,
            gridVoltageB, gridCurrentB, gridFrequencyB, activePowerB,
            gridVoltageC, gridCurrentC, gridFrequencyC, activePowerC,
            totalActivePower,
            pv1Voltage, pv1Current, pv1Power,
            pv2Voltage, pv2Current, pv2Power,
            energyToday, pv1EnergyToday, pv2EnergyToday,
            energyTotal
        )
    }

    // Phase A
    val gridVoltageA = FloatModbusRegister("gridVoltageA", 507, 2) { it * 0.1f }             // 0.1 V
    val gridCurrentA = FloatModbusRegister("gridCurrentA", 510, 2) { it * 0.01f }            // 0.01 A
    val gridFrequencyA = FloatModbusRegister("gridFrequencyA", 513, 2) { it * 0.01f }        // 0.01 Hz
    val activePowerA = IntModbusRegister("activePowerA", 516, 4)                             // W

    // Phase B
    val gridVoltageB = FloatModbusRegister("gridVoltageB", 508, 2) { it * 0.1f }             // 0.1 V
    val gridCurrentB = FloatModbusRegister("gridCurrentB", 511, 2) { it * 0.01f }            // 0.01 A
    val gridFrequencyB = FloatModbusRegister("gridFrequencyB", 514, 2) { it * 0.01f }        // 0.01 Hz
    val activePowerB = IntModbusRegister("activePowerB", 518, 4)                             // W

    // Phase C
    val gridVoltageC = FloatModbusRegister("gridVoltageC", 509, 2) { it * 0.1f }             // 0.1 V
    val gridCurrentC = FloatModbusRegister("gridCurrentC", 512, 2) { it * 0.01f }            // 0.01 A
    val gridFrequencyC = FloatModbusRegister("gridFrequencyC", 515, 2) { it * 0.01f }        // 0.01 Hz
    val activePowerC = IntModbusRegister("activePowerC", 520, 4)                             // W

    // Total output
    val totalActivePower = IntModbusRegister("totalActivePower", 522, 4)                     // W

    // Energy counters
    val energyToday = BigDecimalModbusRegister("energyToday", 1000, 2) { it.divTen() }       // 0.1 kWh
    val energyTotal = BigDecimalModbusRegister("energyTotal", 1014, 4) { it.divTen() }       // 0.1 kWh

    // PV 1
    val pv1Voltage = FloatModbusRegister("pv1Voltage", 555, 2) { it * 0.1f }                 // 0.1 V
    val pv1Current = FloatModbusRegister("pv1Current", 556, 2) { it * 0.01f }                // 0.01 A
    val pv1Power = IntModbusRegister("pv1Power", 557, 2)                                     // W
    val pv1EnergyToday = BigDecimalModbusRegister("pv1EnergyToday", 1008, 2) { it.divTen() } // 0.1 kWh

    // PV 2
    val pv2Voltage = FloatModbusRegister("pv2Voltage", 558, 2) { it * 0.1f }                 // 0.1 V
    val pv2Current = FloatModbusRegister("pv2Current", 559, 2) { it * 0.01f }                // 0.01 A
    val pv2Power = IntModbusRegister("pv2Power", 560, 2)                                     // W
    val pv2EnergyToday = BigDecimalModbusRegister("pv2EnergyToday", 1009, 2) { it.divTen() } // 0.1 kWh

    private fun BigDecimal.divTen(): BigDecimal = divide(BigDecimal("10"))
}