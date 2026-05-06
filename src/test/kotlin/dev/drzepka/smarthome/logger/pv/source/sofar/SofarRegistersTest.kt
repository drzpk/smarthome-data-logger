package dev.drzepka.smarthome.logger.pv.source.sofar

import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusDataFrame
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

class SofarRegistersTest {

    @Test
    fun `should decode register data`() {
        val bytes = buildByteArray(REGISTERS)
        val frame = ModbusDataFrame(0, SofarRegisters.registers)
        val r = SofarRegisters

        val data = frame.decodeResponse(bytes)

        then(data[r.pv1Voltage] as Float).isEqualTo(404.0f, Offset.offset(0.1f))
        then(data[r.pv1Current] as Float).isEqualTo(6.22f, Offset.offset(0.01f))
        then(data[r.pv2Voltage] as Float).isEqualTo(79.7f, Offset.offset(0.1f))
        then(data[r.pv2Current] as Float).isEqualTo(0.01f, Offset.offset(0.01f))
        then(data[r.pv1Power] as Int).isEqualTo(2510)
        then(data[r.pv2Power] as Int).isEqualTo(0)

        then(data[r.phaseAVoltage] as Float).isEqualTo(243.1f, Offset.offset(0.1f))
        then(data[r.phaseACurrent] as Float).isEqualTo(3.50f, Offset.offset(0.01f))
        then(data[r.phaseBVoltage] as Float).isEqualTo(241.0f, Offset.offset(0.1f))
        then(data[r.phaseBCurrent] as Float).isEqualTo(3.51f, Offset.offset(0.01f))
        then(data[r.phaseCVoltage] as Float).isEqualTo(238.2f, Offset.offset(0.1f))
        then(data[r.phaseCCurrent] as Float).isEqualTo(3.49f, Offset.offset(0.01f))

        then(data[r.totalProduction] as Int).isEqualTo(3209000)
        then(data[r.totalGenerationTime] as Int).isEqualTo(3155)
        then(data[r.todayProduction] as Int).isEqualTo(10350)
        then(data[r.todayGenerationTime] as Float).isEqualTo(369 / 60f, Offset.offset(0.1f))
    }

    private fun buildByteArray(registers: IntArray): ByteArray {
        val bytes = ByteArray(registers.size * 2)
        registers.forEachIndexed { i, reg ->
            bytes[i * 2] = (reg shr 8).toByte()
            bytes[i * 2 + 1] = reg.toByte()
        }
        return bytes
    }

    companion object {
        private val REGISTERS = intArrayOf(
            // State (registers 0-5)
            2,    // Operating state
            0,    // Fault 1
            0,    // Fault 2
            0,    // Fault 3
            0,    // Fault 4
            0,    // Fault 5
            // PV input (registers 6-11)
            4040, // PV1 voltage [0.1 V]
            622,  // PV1 current [0.01 A]
            797,  // PV2 voltage [0.1 V]
            1,    // PV2 current [0.01 A]
            251,  // PV1 power   [0.01 kW]
            0,    // PV2 power   [0.01 kW]
            // Grid output (registers 12-20)
            243,  // Output active power [0.01 kW]
            68,   // Output reactive power [0.01 kVar]
            4998, // Grid frequency [0.01 Hz]
            2431, // A-phase voltage [0.1 V]
            350,  // A-phase current [0.01 A]
            2410, // B-phase voltage [0.1 V]
            351,  // B-phase current [0.01 A]
            2382, // C-phase voltage [0.1 V]
            349,  // C-phase current [0.01 A]
            // Energy counters (registers 21-26)
            0,    // Total production high word [1 kWh]
            3209, // Total production low word [1 kWh]
            0,    // Total generation time high word [1 hour]
            3155, // Total generation time low word [1 hour]
            1035, // Today's production [0.01 kWh]
            369,  // Today's generation time [1 minute]
        )
    }
}
