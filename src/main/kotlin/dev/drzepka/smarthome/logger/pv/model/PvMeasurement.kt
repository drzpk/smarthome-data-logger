package dev.drzepka.smarthome.logger.pv.model

import dev.drzepka.smarthome.logger.core.model.server.Measurement
import java.math.BigDecimal
import java.time.Instant

data class PvMeasurement(
    override val deviceId: Int,
    val time: Instant?,
    val totalPower: Int,
    val energyToday: BigDecimal,
    val energyTotal: BigDecimal,
    val phaseA: Phase,
    val phaseB: Phase,
    val phaseC: Phase,
    val pv1: Pv?,
    val pv2: Pv?
) : Measurement

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
