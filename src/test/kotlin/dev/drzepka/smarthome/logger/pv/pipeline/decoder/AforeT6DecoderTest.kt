package dev.drzepka.smarthome.logger.pv.pipeline.decoder

import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusRegister
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusRegisterData
import dev.drzepka.smarthome.logger.pv.vendor.afore.AforeT6Registers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class AforeT6DecoderTest {

    private val r = AforeT6Registers

    @Test
    fun `should decode all registers into PvMeasurement`() {
        val result = AforeT6Decoder.decode(fullRegisterData()).toList()

        assertEquals(1, result.size)
        val m = result[0]

        assertEquals(6930, m.totalPower)
        assertEquals(BigDecimal(100), m.energyToday)
        assertEquals(BigDecimal(10000), m.energyTotal)

        assertEquals(230.0f, m.phaseA.voltage)
        assertEquals(10.0f, m.phaseA.current)
        assertEquals(50.0f, m.phaseA.frequency)
        assertEquals(2300, m.phaseA.power)

        assertEquals(231.0f, m.phaseB.voltage)
        assertEquals(10.1f, m.phaseB.current)
        assertEquals(50.1f, m.phaseB.frequency)
        assertEquals(2310, m.phaseB.power)

        assertEquals(232.0f, m.phaseC.voltage)
        assertEquals(10.2f, m.phaseC.current)
        assertEquals(50.2f, m.phaseC.frequency)
        assertEquals(2320, m.phaseC.power)

        val pv1 = m.pv1!!
        assertEquals(350.0f, pv1.voltage)
        assertEquals(5.0f, pv1.current)
        assertEquals(1750, pv1.power)
        assertEquals(BigDecimal(50), pv1.energyToday)

        val pv2 = m.pv2!!
        assertEquals(360.0f, pv2.voltage)
        assertEquals(5.1f, pv2.current)
        assertEquals(1836, pv2.power)
        assertEquals(BigDecimal(60), pv2.energyToday)
    }

    @Test
    fun `should return empty collection when registers are missing`() {
        val incomplete: ModbusRegisterData = mapOf(r.gridVoltageA to 230.0f)

        val result = AforeT6Decoder.decode(incomplete)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return empty collection when input is empty`() {
        val result = AforeT6Decoder.decode(emptyMap<ModbusRegister<*>, Any>())

        assertTrue(result.isEmpty())
    }

    private fun fullRegisterData(): ModbusRegisterData = mapOf(
        r.gridVoltageA to 230.0f, r.gridCurrentA to 10.0f, r.gridFrequencyA to 50.0f, r.activePowerA to 2300,
        r.gridVoltageB to 231.0f, r.gridCurrentB to 10.1f, r.gridFrequencyB to 50.1f, r.activePowerB to 2310,
        r.gridVoltageC to 232.0f, r.gridCurrentC to 10.2f, r.gridFrequencyC to 50.2f, r.activePowerC to 2320,
        r.totalActivePower to 6930,
        r.energyToday to BigDecimal(100),
        r.energyTotal to BigDecimal(10000),
        r.pv1Voltage to 350.0f, r.pv1Current to 5.0f, r.pv1Power to 1750, r.pv1EnergyToday to 50,
        r.pv2Voltage to 360.0f, r.pv2Current to 5.1f, r.pv2Power to 1836, r.pv2EnergyToday to 60
    )
}
