package dev.drzepka.smarthome.logger.pv.source.sofar

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusRegister
import dev.drzepka.smarthome.logger.core.frame.modbus.getTyped
import dev.drzepka.smarthome.logger.core.model.measurement.Measurement
import dev.drzepka.smarthome.logger.core.model.measurement.Phase
import dev.drzepka.smarthome.logger.core.model.measurement.Pv
import dev.drzepka.smarthome.logger.core.model.measurement.PvMeasurement
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import dev.drzepka.smarthome.logger.pv.common.PvData
import java.math.BigDecimal
import java.time.Instant

object SofarDecoder : DataDecoder<PvData> {
    private val log by Logger()

    override fun decode(item: PvData): Collection<Measurement> {
        val r = SofarRegisters
        val required = listOf(
            r.pv1Voltage, r.pv1Current, r.pv1Power,
            r.pv2Voltage, r.pv2Current, r.pv2Power,
            r.activePower, r.gridFrequency,
            r.phaseAVoltage, r.phaseACurrent,
            r.phaseBVoltage, r.phaseBCurrent,
            r.phaseCVoltage, r.phaseCCurrent,
            r.totalProduction, r.totalGenerationTime,
            r.todayProduction, r.todayGenerationTime
        )

        val missing = required.filter { it !in item.registerData }
        if (missing.isNotEmpty()) {
            missing.forEach { log.warn("Missing register data for: {}", it.name) }
            return emptyList()
        }

        fun <T : Any> get(register: ModbusRegister<T>): T = item.registerData.getTyped(register)

        val frequency = get(r.gridFrequency)
        val measurement = PvMeasurement(
            mac = item.deviceId,
            time = Instant.now(),
            totalPower = get(r.activePower),
            energyToday = BigDecimal(get(r.todayProduction)).divide(BigDecimal(1000)),
            energyTotal = BigDecimal(get(r.totalProduction)).divide(BigDecimal(1000)),
            phaseA = Phase(
                voltage = get(r.phaseAVoltage),
                current = get(r.phaseACurrent),
                power = (get(r.phaseAVoltage) * get(r.phaseACurrent)).toInt(),
                frequency = frequency
            ),
            phaseB = Phase(
                voltage = get(r.phaseBVoltage),
                current = get(r.phaseBCurrent),
                power = (get(r.phaseBVoltage) * get(r.phaseBCurrent)).toInt(),
                frequency = frequency
            ),
            phaseC = Phase(
                voltage = get(r.phaseCVoltage),
                current = get(r.phaseCCurrent),
                power = (get(r.phaseCVoltage) * get(r.phaseCCurrent)).toInt(),
                frequency = frequency
            ),
            pv1 = Pv(
                voltage = get(r.pv1Voltage),
                current = get(r.pv1Current),
                power = get(r.pv1Power),
                energyToday = BigDecimal.ZERO // TODO: per-string energy today not provided by Sofar
            ),
            pv2 = Pv(
                voltage = get(r.pv2Voltage),
                current = get(r.pv2Current),
                power = get(r.pv2Power),
                energyToday = BigDecimal.ZERO // TODO: per-string energy today not provided by Sofar
            )
        )
        return listOf(measurement)
    }
}
