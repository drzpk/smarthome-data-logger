package dev.drzepka.smarthome.logger.core.model.measurement

import java.math.BigDecimal
import java.time.Instant

data class PvMeasurement(
    override val mac: String,
    val time: Instant?,
    val totalPower: Int,
    val energyToday: BigDecimal,
    val energyTotal: BigDecimal,
    val phaseA: Phase,
    val phaseB: Phase,
    val phaseC: Phase,
    val pv1: Pv?,
    val pv2: Pv?
) : Measurement {
    override val type = MeasurementType.PV
}

data class Phase(
    val voltage: Float,
    val current: Float,
    val power: Int?,
    val frequency: Float
)

data class Pv(
    val voltage: Float,
    val current: Float,
    val power: Int,
    val energyToday: BigDecimal
)
