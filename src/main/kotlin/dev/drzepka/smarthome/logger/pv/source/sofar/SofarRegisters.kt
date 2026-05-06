package dev.drzepka.smarthome.logger.pv.source.sofar

import dev.drzepka.smarthome.logger.core.frame.modbus.FloatModbusRegister
import dev.drzepka.smarthome.logger.core.frame.modbus.IntModbusRegister
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusRegister

object SofarRegisters {

    val registers: List<ModbusRegister<*>> by lazy {
        listOf(
            pv1Voltage, pv1Current, pv1Power,
            pv2Voltage, pv2Current, pv2Power,
            activePower, gridFrequency,
            phaseAVoltage, phaseACurrent,
            phaseBVoltage, phaseBCurrent,
            phaseCVoltage, phaseCCurrent,
            totalProduction, totalGenerationTime,
            todayProduction, todayGenerationTime
        )
    }

    // PV 1
    val pv1Voltage = FloatModbusRegister("pv1Voltage", 6, 2) { it * 0.1f }          // 0.1 V
    val pv1Current = FloatModbusRegister("pv1Current", 7, 2) { it * 0.01f }         // 0.01 A
    val pv1Power = IntModbusRegister("pv1Power", 10, 2) { it * 10 }                 // 0.01 kW → W

    // PV 2
    val pv2Voltage = FloatModbusRegister("pv2Voltage", 8, 2) { it * 0.1f }          // 0.1 V
    val pv2Current = FloatModbusRegister("pv2Current", 9, 2) { it * 0.01f }         // 0.01 A
    val pv2Power = IntModbusRegister("pv2Power", 11, 2) { it * 10 }                 // 0.01 kW → W

    // Grid output
    val activePower = IntModbusRegister("activePower", 12, 2) { it * 10 }           // 0.01 kW → W
    val gridFrequency = FloatModbusRegister("gridFrequency", 14, 2) { it * 0.01f }  // 0.01 Hz

    // Phase A
    val phaseAVoltage = FloatModbusRegister("phaseAVoltage", 15, 2) { it * 0.1f }   // 0.1 V
    val phaseACurrent = FloatModbusRegister("phaseACurrent", 16, 2) { it * 0.01f }  // 0.01 A

    // Phase B
    val phaseBVoltage = FloatModbusRegister("phaseBVoltage", 17, 2) { it * 0.1f }   // 0.1 V
    val phaseBCurrent = FloatModbusRegister("phaseBCurrent", 18, 2) { it * 0.01f }  // 0.01 A

    // Phase C
    val phaseCVoltage = FloatModbusRegister("phaseCVoltage", 19, 2) { it * 0.1f }   // 0.1 V
    val phaseCCurrent = FloatModbusRegister("phaseCCurrent", 20, 2) { it * 0.01f }  // 0.01 A

    // Energy counters
    val totalProduction = IntModbusRegister("totalProduction", 21, 4) { it * 1000 } // 1 kWh → Wh
    val totalGenerationTime = IntModbusRegister("totalGenerationTime", 23, 4)        // 1 hour
    val todayProduction = IntModbusRegister("todayProduction", 25, 2) { it * 10 }   // 0.01 kWh → Wh
    val todayGenerationTime = FloatModbusRegister("todayGenerationTime", 26, 2) { it / 60f } // min → h
}
