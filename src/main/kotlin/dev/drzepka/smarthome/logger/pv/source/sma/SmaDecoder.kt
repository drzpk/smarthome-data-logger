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
        val measurement = PvMeasurement(
            mac = mac,
            time = item.time,
            totalPower = int(f.gridPower) ?: 0,
            energyToday = BigDecimal(int(f.dailyYield) ?: 0).divide(BigDecimal(1000)),
            energyTotal = BigDecimal.valueOf(double(f.totalYield) ?: 0.0),
            phaseA = Phase(
                voltage = float(f.voltageL1) ?: 0f,
                current = float(f.currentL1) ?: 0f,
                power = int(f.powerL1),
                frequency = frequency
            ),
            phaseB = Phase(
                voltage = float(f.voltageL2) ?: 0f,
                current = float(f.currentL2) ?: 0f,
                power = int(f.powerL2),
                frequency = frequency
            ),
            phaseC = Phase(
                voltage = float(f.voltageL3) ?: 0f,
                current = float(f.currentL3) ?: 0f,
                power = int(f.powerL3),
                frequency = frequency
            ),
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
