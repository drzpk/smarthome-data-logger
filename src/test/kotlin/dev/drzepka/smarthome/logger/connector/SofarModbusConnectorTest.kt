package dev.drzepka.smarthome.logger.connector

import dev.drzepka.smarthome.common.pvstats.model.vendor.SofarData
import dev.drzepka.smarthome.logger.model.config.source.SofarModbusConfig
import dev.drzepka.smarthome.logger.util.PropertiesLoader
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class SofarModbusConnectorTest {

    @Test
    fun `should parse modbus response`() {
        val connector = SofarModbusConnector(SofarModbusConfig("name", getPropertiesLoader()))

        val sofarData = connector.getVendorData(REGISTERS) as SofarData

        Assertions.assertEquals(404.0f, sofarData.pv1Voltage, 0.1f)
        Assertions.assertEquals(6.22f, sofarData.pv1Current, 0.01f)
        Assertions.assertEquals(79.7f, sofarData.pv2Voltage, 0.1f)
        Assertions.assertEquals(0.01f, sofarData.pv2Current, 0.01f)
        Assertions.assertEquals(2510, sofarData.pv1Power)
        Assertions.assertEquals(0, sofarData.pv2Power)

        Assertions.assertEquals(243.1f, sofarData.phaseAVoltage, 0.1f)
        Assertions.assertEquals(3.50f, sofarData.phaseACurrent, 0.01f)
        Assertions.assertEquals(241.0f, sofarData.phaseBVoltage, 0.1f)
        Assertions.assertEquals(3.51f, sofarData.phaseBCurrent, 0.01f)
        Assertions.assertEquals(238.2f, sofarData.phaseCVoltage, 0.1f)
        Assertions.assertEquals(3.49f, sofarData.phaseCCurrent, 0.01f)

        Assertions.assertEquals(3209000, sofarData.energyTotal)
        Assertions.assertEquals(3155, sofarData.generationHoursTotal)
        Assertions.assertEquals(10350, sofarData.energyToday)
        Assertions.assertEquals(369 / 60f, sofarData.generationHoursToday, 0.1f)
    }

    private fun getPropertiesLoader(): PropertiesLoader {
        val properties = Properties()
        properties.setProperty("source.name.user", "user")
        properties.setProperty("source.name.password", "password")
        properties.setProperty("source.name.timeout", "3")
        properties.setProperty("source.name.devpath", "/dev/ttyUSB0")
        properties.setProperty("source.name.slaveId", "0")

        return PropertiesLoader(properties)
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