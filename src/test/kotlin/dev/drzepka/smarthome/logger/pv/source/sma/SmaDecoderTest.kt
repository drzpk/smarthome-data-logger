package dev.drzepka.smarthome.logger.pv.source.sma

import dev.drzepka.smarthome.logger.core.model.measurement.PvMeasurement
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

internal class SmaDecoderTest {

    private val decoder = SmaDecoder("sma-host")
    private val time = Instant.parse("2024-06-01T10:00:00Z")

    @Test
    fun `should map mac from constructor`() {
        val result = decoder.decode(data()).first() as PvMeasurement

        then(result.mac).isEqualTo("sma-host")
    }

    @Test
    fun `should map gridPower to totalPower`() {
        val result = decoder.decode(data(SmaFields.gridPower to 4200.0)).first() as PvMeasurement

        then(result.totalPower).isEqualTo(4200)
        then(result.time).isEqualTo(time)
    }

    @Test
    fun `should map dailyYield Wh to energyToday kWh`() {
        val result = decoder.decode(data(SmaFields.dailyYield to 7800.0)).first() as PvMeasurement

        then(result.energyToday).isEqualByComparingTo(BigDecimal("7.8"))
    }

    @Test
    fun `should map totalYield to energyTotal`() {
        val result = decoder.decode(data(SmaFields.totalYield to 491.919)).first() as PvMeasurement

        then(result.energyTotal).isEqualByComparingTo(BigDecimal("491.919"))
    }

    @Test
    fun `should default to zero when numeric fields are absent`() {
        val result = decoder.decode(data()).first() as PvMeasurement

        then(result.totalPower).isEqualTo(0)
        then(result.energyToday).isEqualByComparingTo(BigDecimal.ZERO)
        then(result.energyTotal).isEqualByComparingTo(BigDecimal.ZERO)
    }

    @Test
    fun `should map phase voltage current power and frequency`() {
        val result = decoder.decode(data(
            SmaFields.voltageL1 to 235.5, SmaFields.currentL1 to 3.2, SmaFields.powerL1 to 753.0,
            SmaFields.voltageL2 to 234.0, SmaFields.currentL2 to 3.1, SmaFields.powerL2 to 725.0,
            SmaFields.voltageL3 to 236.0, SmaFields.currentL3 to 3.0, SmaFields.powerL3 to 708.0,
            SmaFields.frequency to 50.0
        )).first() as PvMeasurement

        then(result.phaseA.voltage).isEqualTo(235.5f)
        then(result.phaseA.current).isEqualTo(3.2f)
        then(result.phaseA.power).isEqualTo(753)
        then(result.phaseA.frequency).isEqualTo(50.0f)

        then(result.phaseB.power).isEqualTo(725)
        then(result.phaseC.power).isEqualTo(708)
    }

    @Test
    fun `should set pv1 and pv2 to null when pvPowerA and pvPowerB are absent`() {
        val result = decoder.decode(data()).first() as PvMeasurement

        then(result.pv1).isNull()
        then(result.pv2).isNull()
    }

    @Test
    fun `should populate pv1 and pv2 when pvPower fields are present`() {
        val result = decoder.decode(data(
            SmaFields.pvPowerA to 1100.0, SmaFields.pvVoltageA to 380.0, SmaFields.pvCurrentA to 2.9,
            SmaFields.pvPowerB to 950.0,  SmaFields.pvVoltageB to 375.0, SmaFields.pvCurrentB to 2.5
        )).first() as PvMeasurement

        then(result.pv1?.power).isEqualTo(1100)
        then(result.pv1?.voltage).isEqualTo(380.0f)
        then(result.pv1?.current).isEqualTo(2.9f)

        then(result.pv2?.power).isEqualTo(950)
        then(result.pv2?.voltage).isEqualTo(375.0f)
    }

    private fun data(vararg entries: Pair<SmaField, Double>) =
        SmaData(time, mapOf(*entries))
}
