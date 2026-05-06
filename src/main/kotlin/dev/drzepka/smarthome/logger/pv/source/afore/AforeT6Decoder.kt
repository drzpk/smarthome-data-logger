package dev.drzepka.smarthome.logger.pv.source.afore

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusRegister
import dev.drzepka.smarthome.logger.core.frame.modbus.getTyped
import dev.drzepka.smarthome.logger.core.model.measurement.Phase
import dev.drzepka.smarthome.logger.core.model.measurement.Pv
import dev.drzepka.smarthome.logger.core.model.measurement.PvMeasurement
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import dev.drzepka.smarthome.logger.pv.common.PvData
import java.time.Instant

object AforeT6Decoder : DataDecoder<PvData> {
    private val log by Logger()

    override fun decode(item: PvData): Collection<PvMeasurement> {
        val r = AforeT6Registers
        val required = listOf(
            r.gridVoltageA, r.gridCurrentA, r.gridFrequencyA, r.activePowerA,
            r.gridVoltageB, r.gridCurrentB, r.gridFrequencyB, r.activePowerB,
            r.gridVoltageC, r.gridCurrentC, r.gridFrequencyC, r.activePowerC,
            r.totalActivePower, r.energyToday, r.energyTotal,
            r.pv1Voltage, r.pv1Current, r.pv1Power, r.pv1EnergyToday,
            r.pv2Voltage, r.pv2Current, r.pv2Power, r.pv2EnergyToday
        )

        val missing = required.filter { it !in item.registerData }
        if (missing.isNotEmpty()) {
            missing.forEach { log.warn("Missing register data for: {}", it.name) }
            return emptyList()
        }

        fun <T : Any> get(register: ModbusRegister<T>): T = item.registerData.getTyped(register)

        val measurement = PvMeasurement(
            mac = item.deviceId,
            time = Instant.now(),
            totalPower = get(r.totalActivePower),
            energyToday = get(r.energyToday),
            energyTotal = get(r.energyTotal),
            phaseA = Phase(
                voltage = get(r.gridVoltageA),
                current = get(r.gridCurrentA),
                power = get(r.activePowerA),
                frequency = get(r.gridFrequencyA)
            ),
            phaseB = Phase(
                voltage = get(r.gridVoltageB),
                current = get(r.gridCurrentB),
                power = get(r.activePowerB),
                frequency = get(r.gridFrequencyB)
            ),
            phaseC = Phase(
                voltage = get(r.gridVoltageC),
                current = get(r.gridCurrentC),
                power = get(r.activePowerC),
                frequency = get(r.gridFrequencyC)
            ),
            pv1 = Pv(
                voltage = get(r.pv1Voltage),
                current = get(r.pv1Current),
                power = get(r.pv1Power),
                energyToday = get(r.pv1EnergyToday)
            ),
            pv2 = Pv(
                voltage = get(r.pv2Voltage),
                current = get(r.pv2Current),
                power = get(r.pv2Power),
                energyToday = get(r.pv2EnergyToday)
            )
        )

        return listOf(measurement)
    }
}
