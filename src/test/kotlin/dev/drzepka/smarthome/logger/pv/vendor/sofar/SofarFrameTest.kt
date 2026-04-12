package dev.drzepka.smarthome.logger.pv.vendor.sofar

import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

class SofarFrameTest {

    @Test
    fun `should deserialize data`() {
        val frame = SofarFrame().decodeResponse(getRegistersData())

        then(frame.pv1Voltage).isEqualTo(404.0f, Offset.offset(0.1f))
        then(frame.pv1Current).isEqualTo(6.22f, Offset.offset(0.01f))
        then(frame.pv2Voltage).isEqualTo(79.7f, Offset.offset(0.1f))
        then(frame.pv2Current).isEqualTo(0.01f, Offset.offset(0.01f))
        then(frame.pv1Power).isEqualTo(2510)
        then(frame.pv2Power).isEqualTo(0)

        then(frame.phaseAVoltage).isEqualTo(243.1f, Offset.offset(0.1f))
        then(frame.phaseACurrent).isEqualTo(3.50f, Offset.offset(0.01f))
        then(frame.phaseBVoltage).isEqualTo(241.0f, Offset.offset(0.1f))
        then(frame.phaseBCurrent).isEqualTo(3.51f, Offset.offset(0.01f))
        then(frame.phaseCVoltage).isEqualTo(238.2f, Offset.offset(0.1f))
        then(frame.phaseCCurrent).isEqualTo(3.49f, Offset.offset(0.01f))

        then(frame.energyTotal).isEqualTo(3209000)
        then(frame.generationHoursTotal).isEqualTo(3155)
        then(frame.energyToday).isEqualTo(10350)
        then(frame.generationHoursToday).isEqualTo(369 / 60f, Offset.offset(0.1f))
    }

    private fun getRegistersData(): ByteArray {
        val bytes = REGISTERS.flatMap {
            // Big endian
            listOf((it shr 8).toByte(), it.toByte())
        }

        val result = bytes.toMutableList()
        result.add(0, 0)

        return result.toByteArray()
    }

    companion object {
        private val REGISTERS = intArrayOf(
            // State
            2,    // Operating state
            0,    // Fault 1
            0,    // Fault 2
            0,    // Fault 3
            0,    // Fault 4
            0,    // Fault 5
            // PV input message
            4040, // PV1 voltage [0.1 V]
            622,  // PV1 current [0.01 A]
            797,  // PV2 voltage [0.1 V]
            1,    // PV2 current [0.01 A]
            251,  // PV1 power   [0.01 kW]
            0,    // PV2 power   [0.01 kW]
            // Output grid message
            243,  // Output active power [0.1 kW]
            68,   // Output reactive power [0.01 kVar]
            4998, // Grid frequency [0.01 Hz]
            2431, // A-phase voltage [0.1 V]
            350,  // A-phase current [0.01 A]
            2410, // B-phase voltage [0.1 V]
            351,  // B-phase current [0.01 A]
            2382, // C-phase voltage [0.1 V]
            349,  // C-phase current [0.01 A]
            // Inverter generation message
            0,    // Total production high byte [1 kWh]
            3209, // Total productino low byte [1 kWh]
            0,    // Total generation time high byte [1 hour]
            3155, // Total generation time low byte [1 hour]
            1035, // Today's production [0.01 kWh]
            369,  // Today's generation time [1 minute]
            // Inverter inner message
            27,   // Inverter module temperature
            41,   // Inveter inner temperature
            6594, // Inverter bus voltage [0.01 V]
            4037, // PV1 voltage sample by slave CPU [0.1 V]
            809,  // PV1 current sample by slsave CPU [0.01 A]
            60,   // Countdown time
            0,    // Inverter alert message
            1,    // Input mode
            0,    // Communication board inner message
            1272, // Insulation of PV1+ to ground
            2203, // Insulation of PV2+ (??) to ground
            1793, // Country
            // Undocumented
            12,
            0,
            0,
            7,
            990,
            993,
            999,
            0,
            0
        )
    }
}
