package dev.drzepka.smarthome.logger.pv.source.sofar

import dev.drzepka.smarthome.logger.core.model.measurement.Measurement
import dev.drzepka.smarthome.logger.core.model.measurement.Phase
import dev.drzepka.smarthome.logger.core.model.measurement.Pv
import dev.drzepka.smarthome.logger.core.model.measurement.PvMeasurement
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import java.math.BigDecimal

class SofarDecoder(private val mac: String) : DataDecoder<SofarData> {

    override fun decode(item: SofarData): Collection<Measurement> {
        val measurement = PvMeasurement(
            mac = mac,
            time = item.date,
            totalPower = item.currentPower,
            energyToday = BigDecimal(item.energyToday).divide(BigDecimal(1000)),
            energyTotal = BigDecimal(item.energyTotal).divide(BigDecimal(1000)),
            phaseA = Phase(
                voltage = item.phaseAVoltage,
                current = item.phaseACurrent,
                power = (item.phaseAVoltage * item.phaseACurrent).toInt(),
                frequency = item.frequency
            ),
            phaseB = Phase(
                voltage = item.phaseBVoltage,
                current = item.phaseBCurrent,
                power = (item.phaseBVoltage * item.phaseBCurrent).toInt(),
                frequency = item.frequency
            ),
            phaseC = Phase(
                voltage = item.phaseCVoltage,
                current = item.phaseCCurrent,
                power = (item.phaseCVoltage * item.phaseCCurrent).toInt(),
                frequency = item.frequency
            ),
            pv1 = Pv(
                voltage = item.pv1Voltage,
                current = item.pv1Current,
                power = item.pv1Power,
                energyToday = BigDecimal.ZERO // TODO: per-string energy today not provided by Sofar
            ),
            pv2 = Pv(
                voltage = item.pv2Voltage,
                current = item.pv2Current,
                power = item.pv2Power,
                energyToday = BigDecimal.ZERO // TODO: per-string energy today not provided by Sofar
            )
        )
        return listOf(measurement)
    }
}
