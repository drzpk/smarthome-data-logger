package dev.drzepka.smarthome.logger.pv.source.afore

import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusRegisterData
import dev.drzepka.smarthome.logger.pv.common.PvData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class AforeT6DecoderTest {

    private val r = AforeT6Registers

    @Test
    fun `should decode all registers into PvMeasurement`() {
        val result = AforeT6Decoder.decode(PvData("0", fullRegisterData())).toList()

        Assertions.assertEquals(1, result.size)
        val m = result[0]

        Assertions.assertEquals(6930, m.totalPower)
        Assertions.assertEquals(BigDecimal(100), m.energyToday)
        Assertions.assertEquals(BigDecimal(10000), m.energyTotal)

        Assertions.assertEquals(230.0f, m.phaseA.voltage)
        Assertions.assertEquals(10.0f, m.phaseA.current)
        Assertions.assertEquals(50.0f, m.phaseA.frequency)
        Assertions.assertEquals(2300, m.phaseA.power)

        Assertions.assertEquals(231.0f, m.phaseB.voltage)
        Assertions.assertEquals(10.1f, m.phaseB.current)
        Assertions.assertEquals(50.1f, m.phaseB.frequency)
        Assertions.assertEquals(2310, m.phaseB.power)

        Assertions.assertEquals(232.0f, m.phaseC.voltage)
        Assertions.assertEquals(10.2f, m.phaseC.current)
        Assertions.assertEquals(50.2f, m.phaseC.frequency)
        Assertions.assertEquals(2320, m.phaseC.power)

        val pv1 = m.pv1!!
        Assertions.assertEquals(350.0f, pv1.voltage)
        Assertions.assertEquals(5.0f, pv1.current)
        Assertions.assertEquals(1750, pv1.power)
        Assertions.assertEquals(BigDecimal(50), pv1.energyToday)

        val pv2 = m.pv2!!
        Assertions.assertEquals(360.0f, pv2.voltage)
        Assertions.assertEquals(5.1f, pv2.current)
        Assertions.assertEquals(1836, pv2.power)
        Assertions.assertEquals(BigDecimal(60), pv2.energyToday)
    }

    @Test
    fun `should return empty collection when registers are missing`() {
        val incomplete: ModbusRegisterData = mapOf(r.gridVoltageA to 230.0f)

        val result = AforeT6Decoder.decode(PvData("0", incomplete))

        Assertions.assertTrue(result.isEmpty())
    }

    @Test
    fun `should return empty collection when input is empty`() {
        val result = AforeT6Decoder.decode(PvData("0", emptyMap()))

        Assertions.assertTrue(result.isEmpty())
    }

    private fun fullRegisterData(): ModbusRegisterData = mapOf(
        r.gridVoltageA to 230.0f, r.gridCurrentA to 10.0f, r.gridFrequencyA to 50.0f, r.activePowerA to 2300,
        r.gridVoltageB to 231.0f, r.gridCurrentB to 10.1f, r.gridFrequencyB to 50.1f, r.activePowerB to 2310,
        r.gridVoltageC to 232.0f, r.gridCurrentC to 10.2f, r.gridFrequencyC to 50.2f, r.activePowerC to 2320,
        r.totalActivePower to 6930,
        r.energyToday to BigDecimal(100),
        r.energyTotal to BigDecimal(10000),
        r.pv1Voltage to 350.0f, r.pv1Current to 5.0f, r.pv1Power to 1750, r.pv1EnergyToday to BigDecimal(50),
        r.pv2Voltage to 360.0f, r.pv2Current to 5.1f, r.pv2Power to 1836, r.pv2EnergyToday to BigDecimal(60)
    )
}