package dev.drzepka.smarthome.logger.pv.source.sofar

import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusRegisterData
import dev.drzepka.smarthome.logger.core.model.measurement.PvMeasurement
import dev.drzepka.smarthome.logger.pv.common.PvData
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

internal class SofarDecoderTest {

    @Test
    fun `should map mac and time`() {
        val before = Instant.now()
        val data = sofarData()

        val result = SofarDecoder.decode(data)

        then(result).hasSize(1)
        val m = result.first() as PvMeasurement
        then(m.mac).isEqualTo("test-mac")
        then(m.time).isBetween(before, Instant.now().plusSeconds(1))
    }

    @Test
    fun `should map current power`() {
        val data = sofarData(activePower = 3500)

        val result = SofarDecoder.decode(data).first() as PvMeasurement

        then(result.totalPower).isEqualTo(3500)
    }

    @Test
    fun `should convert energy today from Wh to kWh`() {
        val data = sofarData(todayProductionWh = 12500)

        val result = SofarDecoder.decode(data).first() as PvMeasurement

        then(result.energyToday).isEqualByComparingTo(BigDecimal("12.5"))
    }

    @Test
    fun `should convert energy total from Wh to kWh`() {
        val data = sofarData(totalProductionWh = 5_000_000)

        val result = SofarDecoder.decode(data).first() as PvMeasurement

        then(result.energyTotal).isEqualByComparingTo(BigDecimal("5000"))
    }

    @Test
    fun `should map phase voltages and currents`() {
        val data = sofarData(
            phaseAVoltage = 230.1f, phaseACurrent = 5.2f,
            phaseBVoltage = 231.0f, phaseBCurrent = 5.0f,
            phaseCVoltage = 229.5f, phaseCCurrent = 4.8f
        )

        val result = SofarDecoder.decode(data).first() as PvMeasurement

        then(result.phaseA?.voltage).isEqualTo(230.1f)
        then(result.phaseA?.current).isEqualTo(5.2f)
        then(result.phaseA?.power).isEqualTo((230.1f * 5.2f).toInt())
        then(result.phaseB?.voltage).isEqualTo(231.0f)
        then(result.phaseC?.current).isEqualTo(4.8f)
        then(result.phaseC?.power).isEqualTo((229.5f * 4.8f).toInt())
    }

    @Test
    fun `should use global frequency for all phases`() {
        val data = sofarData(gridFrequency = 50.01f)

        val result = SofarDecoder.decode(data).first() as PvMeasurement

        then(result.phaseA?.frequency).isEqualTo(50.01f)
        then(result.phaseB?.frequency).isEqualTo(50.01f)
        then(result.phaseC?.frequency).isEqualTo(50.01f)
    }

    @Test
    fun `should map pv string voltage, current and power`() {
        val data = sofarData(
            pv1Voltage = 380.5f, pv1Current = 9.1f, pv1Power = 3460,
            pv2Voltage = 375.0f, pv2Current = 8.9f, pv2Power = 3337
        )

        val result = SofarDecoder.decode(data).first() as PvMeasurement

        then(result.pv1?.voltage).isEqualTo(380.5f)
        then(result.pv1?.current).isEqualTo(9.1f)
        then(result.pv1?.power).isEqualTo(3460)
        then(result.pv2?.voltage).isEqualTo(375.0f)
        then(result.pv2?.power).isEqualTo(3337)
    }

    @Test
    fun `should set pv string energy today to zero`() {
        val data = sofarData()

        val result = SofarDecoder.decode(data).first() as PvMeasurement

        then(result.pv1!!.energyToday).isEqualByComparingTo(BigDecimal.ZERO)
        then(result.pv2!!.energyToday).isEqualByComparingTo(BigDecimal.ZERO)
    }

    private fun sofarData(
        mac: String = "test-mac",
        activePower: Int = 0,
        todayProductionWh: Int = 0,
        totalProductionWh: Int = 0,
        gridFrequency: Float = 50.0f,
        phaseAVoltage: Float = 230f, phaseACurrent: Float = 0f,
        phaseBVoltage: Float = 230f, phaseBCurrent: Float = 0f,
        phaseCVoltage: Float = 230f, phaseCCurrent: Float = 0f,
        pv1Voltage: Float = 0f, pv1Current: Float = 0f, pv1Power: Int = 0,
        pv2Voltage: Float = 0f, pv2Current: Float = 0f, pv2Power: Int = 0,
        todayGenerationTime: Float = 3.5f,
        totalGenerationTime: Int = 1200
    ): PvData {
        val r = SofarRegisters
        val registerData: ModbusRegisterData = mapOf(
            r.activePower to activePower,
            r.todayProduction to todayProductionWh,
            r.totalProduction to totalProductionWh,
            r.gridFrequency to gridFrequency,
            r.phaseAVoltage to phaseAVoltage,
            r.phaseACurrent to phaseACurrent,
            r.phaseBVoltage to phaseBVoltage,
            r.phaseBCurrent to phaseBCurrent,
            r.phaseCVoltage to phaseCVoltage,
            r.phaseCCurrent to phaseCCurrent,
            r.pv1Voltage to pv1Voltage,
            r.pv1Current to pv1Current,
            r.pv1Power to pv1Power,
            r.pv2Voltage to pv2Voltage,
            r.pv2Current to pv2Current,
            r.pv2Power to pv2Power,
            r.todayGenerationTime to todayGenerationTime,
            r.totalGenerationTime to totalGenerationTime
        )
        return PvData(mac, registerData)
    }
}
