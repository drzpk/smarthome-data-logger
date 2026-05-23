package dev.drzepka.smarthome.logger.pv.source.sma

import dev.drzepka.smarthome.logger.core.model.measurement.Measurement
import dev.drzepka.smarthome.logger.core.model.measurement.Phase
import dev.drzepka.smarthome.logger.core.model.measurement.Pv
import dev.drzepka.smarthome.logger.core.model.measurement.PvMeasurement
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import java.math.BigDecimal

class SmaDecoder(private val mac: String) : DataDecoder<SmaData> {

    override fun decode(item: SmaData): Collection<Measurement> {
        fun double(field: SmaField) = item.values[field]
        fun int(field: SmaField) = double(field)?.toInt()
        fun float(field: SmaField) = double(field)?.toFloat()

        val f = SmaFields
        val frequency = float(f.frequency) ?: 0f
        fun phase(voltage: SmaField, current: SmaField, power: SmaField): Phase? {
            if (double(voltage) == null && double(current) == null && double(power) == null) return null
            return Phase(
                voltage = float(voltage) ?: 0f,
                current = float(current) ?: 0f,
                power = int(power),
                frequency = frequency
            )
        }
        val measurement = PvMeasurement(
            mac = mac,
            time = item.time,
            totalPower = int(f.gridPower) ?: 0,
            energyToday = BigDecimal(int(f.dailyYield) ?: 0).divide(BigDecimal(1000)),
            energyTotal = BigDecimal.valueOf(double(f.totalYield) ?: 0.0),
            phaseA = phase(f.voltageL1, f.currentL1, f.powerL1),
            phaseB = phase(f.voltageL2, f.currentL2, f.powerL2),
            phaseC = phase(f.voltageL3, f.currentL3, f.powerL3),
            pv1 = int(f.pvPowerA)?.let {
                Pv(
                    voltage = float(f.pvVoltageA) ?: 0f,
                    current = float(f.pvCurrentA) ?: 0f,
                    power = it,
                    energyToday = BigDecimal.ZERO
                )
            },
            pv2 = int(f.pvPowerB)?.let {
                Pv(
                    voltage = float(f.pvVoltageB) ?: 0f,
                    current = float(f.pvCurrentB) ?: 0f,
                    power = it,
                    energyToday = BigDecimal.ZERO
                )
            }
        )
        return listOf(measurement)
    }
}
