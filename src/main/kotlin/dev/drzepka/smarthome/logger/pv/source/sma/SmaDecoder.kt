package dev.drzepka.smarthome.logger.pv.source.sma

import dev.drzepka.smarthome.logger.core.model.measurement.Measurement
import dev.drzepka.smarthome.logger.core.model.measurement.Phase
import dev.drzepka.smarthome.logger.core.model.measurement.PvMeasurement
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import java.math.BigDecimal

class SmaDecoder(private val mac: String) : DataDecoder<SmaData> {

    override fun decode(item: SmaData): Collection<Measurement> {
        val measurement = when (item) {
            is SmaData.Metrics -> PvMeasurement(
                mac = mac,
                time = item.time,
                totalPower = item.power,
                energyToday = BigDecimal.ZERO, // TODO: energy today not available from metrics endpoint
                energyTotal = BigDecimal.ZERO, // TODO: total energy not available from metrics endpoint
                phaseA = DUMMY_PHASE, // TODO: phase data not available from SMA basic API
                phaseB = DUMMY_PHASE,
                phaseC = DUMMY_PHASE,
                pv1 = null, // TODO: PV string data not available from SMA basic API
                pv2 = null
            )
            is SmaData.Measurement -> PvMeasurement(
                mac = mac,
                time = item.time,
                totalPower = 0, // TODO: power not available per measurement entry
                energyToday = BigDecimal(item.energyWh).divide(BigDecimal(1000)),
                energyTotal = BigDecimal.ZERO, // TODO: lifetime total energy not available
                phaseA = DUMMY_PHASE, // TODO: phase data not available from SMA basic API
                phaseB = DUMMY_PHASE,
                phaseC = DUMMY_PHASE,
                pv1 = null,
                pv2 = null
            )
        }
        return listOf(measurement)
    }

    companion object {
        private val DUMMY_PHASE = Phase(voltage = 0f, current = 0f, power = null, frequency = 0f)
    }
}
