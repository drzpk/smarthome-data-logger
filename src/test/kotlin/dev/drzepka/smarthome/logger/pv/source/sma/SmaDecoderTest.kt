package dev.drzepka.smarthome.logger.pv.source.sma

import dev.drzepka.smarthome.logger.core.model.measurement.PvMeasurement
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

internal class SmaDecoderTest {

    private val decoder = SmaDecoder("sma-host")

    @Test
    fun `should map mac from constructor`() {
        val data = SmaData.Metrics(Instant.parse("2024-06-01T10:00:00Z"), 1000)

        val result = decoder.decode(data).first() as PvMeasurement

        then(result.mac).isEqualTo("sma-host")
    }

    @Test
    fun `should map metrics power`() {
        val data = SmaData.Metrics(Instant.parse("2024-06-01T10:00:00Z"), power = 4200)

        val result = decoder.decode(data).first() as PvMeasurement

        then(result.totalPower).isEqualTo(4200)
        then(result.time).isEqualTo(Instant.parse("2024-06-01T10:00:00Z"))
    }

    @Test
    fun `should set energy fields to zero for metrics`() {
        val data = SmaData.Metrics(Instant.parse("2024-06-01T10:00:00Z"), power = 1000)

        val result = decoder.decode(data).first() as PvMeasurement

        then(result.energyToday).isEqualByComparingTo(BigDecimal.ZERO)
        then(result.energyTotal).isEqualByComparingTo(BigDecimal.ZERO)
    }

    @Test
    fun `should set pv strings to null for metrics`() {
        val data = SmaData.Metrics(Instant.parse("2024-06-01T10:00:00Z"), power = 1000)

        val result = decoder.decode(data).first() as PvMeasurement

        then(result.pv1).isNull()
        then(result.pv2).isNull()
    }

    @Test
    fun `should map measurement energy today from Wh to kWh`() {
        val data = SmaData.Measurement(Instant.parse("2024-06-01T14:30:00Z"), energyWh = 7800)

        val result = decoder.decode(data).first() as PvMeasurement

        then(result.energyToday).isEqualByComparingTo(BigDecimal("7.8"))
        then(result.time).isEqualTo(Instant.parse("2024-06-01T14:30:00Z"))
    }

    @Test
    fun `should set total power to zero for measurement`() {
        val data = SmaData.Measurement(Instant.parse("2024-06-01T14:30:00Z"), energyWh = 5000)

        val result = decoder.decode(data).first() as PvMeasurement

        then(result.totalPower).isEqualTo(0)
    }

    @Test
    fun `should set energy total to zero for measurement`() {
        val data = SmaData.Measurement(Instant.parse("2024-06-01T14:30:00Z"), energyWh = 5000)

        val result = decoder.decode(data).first() as PvMeasurement

        then(result.energyTotal).isEqualByComparingTo(BigDecimal.ZERO)
    }
}
