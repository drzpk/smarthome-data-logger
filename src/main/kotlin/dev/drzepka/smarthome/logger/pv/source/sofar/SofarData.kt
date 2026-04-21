package dev.drzepka.smarthome.logger.pv.source.sofar

import java.time.Instant

data class SofarData(
    val date: Instant,
    val energyToday: Int,
    val energyTotal: Int,
    val currentPower: Watts,
    val frequency: Float,
    val generationHoursToday: Float,
    val generationHoursTotal: Int,
    val pv1Voltage: Float,
    val pv1Current: Float,
    val pv1Power: Watts,
    val pv2Voltage: Float,
    val pv2Current: Float,
    val pv2Power: Watts,
    val phaseAVoltage: Float,
    val phaseACurrent: Float,
    val phaseBVoltage: Float,
    val phaseBCurrent: Float,
    val phaseCVoltage: Float,
    val phaseCCurrent: Float
)

typealias Watts = Int
