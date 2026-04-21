package dev.drzepka.smarthome.logger.pv.source.sofar

import dev.drzepka.smarthome.common.pvstats.model.vendor.sofar.SofarDataImpl
import dev.drzepka.smarthome.logger.core.frame.Frame
import java.time.Instant
import java.util.*
import kotlin.math.floor

class SofarFrame : Frame<SofarData> {

    override fun encodeRequest(): ByteArray = byteArrayOf()

    override fun decodeResponse(content: ByteArray): SofarData = SofarData(
        date = Instant.now(),
        energyToday = floor(content.getShort(TODAY_PRODUCTION, 1) * 10).toInt(),
        energyTotal = content.getInt(TOTAL_PRODUCTION) * 1000,
        currentPower = floor(content.getShort(ACTIVE_POWER, 1) * 10).toInt(),
        frequency = content.getShort(GRID_FREQUENCY, 100),
        generationHoursToday = content.getShort(TODAY_GENERATION_TIME, 60),
        generationHoursTotal = content.getInt(TOTAL_GENERATION_TIME),
        pv1Voltage = content.getShort(PV1_VOLTAGE, 10),
        pv1Current = content.getShort(PV1_CURRENT, 100),
        pv1Power = floor(content.getShort(PV1_POWER, 1) * 10).toInt(),
        pv2Voltage = content.getShort(PV2_VOLTAGE, 10),
        pv2Current = content.getShort(PV2_CURRENT, 100),
        pv2Power = floor(content.getShort(PV2_POWER, 1) * 10).toInt(),
        phaseAVoltage = content.getShort(PHASE_A_VOLTAGE, 10),
        phaseACurrent = content.getShort(PHASE_A_CURRENT, 100),
        phaseBVoltage = content.getShort(PHASE_B_VOLTAGE, 10),
        phaseBCurrent = content.getShort(PHASE_B_CURRENT, 100),
        phaseCVoltage = content.getShort(PHASE_C_VOLTAGE, 10),
        phaseCCurrent = content.getShort(PHASE_C_CURRENT, 100)
    )

    private fun ByteArray.getShort(offset: Int, divider: Int): Float =
        this[offset].toInt().and(0xff).shl(8).or(this[offset + 1].toInt().and(0xff)).toFloat() / divider

    private fun ByteArray.getInt(offset: Int): Int =
        this[offset].toInt().and(0xff).shl(24)
            .or(this[offset + 1].toInt().and(0xff).shl(16))
            .or(this[offset + 2].toInt().and(0xff).shl(8))
            .or(this[offset + 3].toInt().and(0xff))

    @Suppress("unused")
    companion object Offsets {
        fun deserialize(data: Any): SofarDataImpl {
            if (data !is String)
                throw IllegalArgumentException("Unknown data type: ${data::class.java.simpleName}")

            val split = data.split(SERIALIZATION_SEPARATOR)
            val date = Instant.ofEpochMilli(split[0].toLong())
            val raw = Base64.getDecoder().decode(split[1])

            return SofarDataImpl(raw.toTypedArray(), date)
        }

        private const val SERIALIZATION_SEPARATOR = ":"

        // Basic info
        private const val OPERATING_STATE = 1
        private const val FAULT_1 = 3
        private const val FAULT_2 = 5
        private const val FAULT_3 = 7
        private const val FAULT_4 = 9
        private const val FAULT_5 = 11

        // Grid input data
        private const val PV1_VOLTAGE = 13 // Unit: 0.1V
        private const val PV1_CURRENT = 15 // Unit: 0.01A
        private const val PV2_VOLTAGE = 17 // Unit: 0.1V
        private const val PV2_CURRENT = 19 // Unit: 0.01A
        private const val PV1_POWER = 21 // Unit: 0.01kW
        private const val PV2_POWER = 23 // Unit: 0.01kW

        // Grid output data
        private const val ACTIVE_POWER = 25 // Unit: 0.01kW
        private const val REACTIVE_POWER = 27 // Unit: 0.01kVar
        private const val GRID_FREQUENCY = 29 // Unit: 0.01Hz
        private const val PHASE_A_VOLTAGE = 31 // Unit: 0.1V
        private const val PHASE_A_CURRENT = 33 // Unit: 0.01A
        private const val PHASE_B_VOLTAGE = 35 // Unit: 0.1V
        private const val PHASE_B_CURRENT = 37 // Unit: 0.01A
        private const val PHASE_C_VOLTAGE = 39 // Unit: 0.1V
        private const val PHASE_C_CURRENT = 41 // Unit: 0.01A

        // Power generation data
        private const val TOTAL_PRODUCTION = 43 // Unit: 1kWh
        private const val TOTAL_GENERATION_TIME = 47 // Unit: 1hour
        private const val TODAY_PRODUCTION = 51 // Unit: 0.01kWh
        private const val TODAY_GENERATION_TIME = 53

        // Internal inverter data
        private const val INVERTER_MODULE_TEMPERATURE = 55
        private const val INVERTER_INNER_TEMPERATURE = 57
        private const val INVERTER_BUS_VOLTAGE = 59
        private const val PV1_VOLTAGE_SAMPLE_BY_SLAVE_CPU = 61 // Unit: 0.1V
        private const val PV1_VOLGATE_SAMPLE_BY_SLAVE_CPU = 63 // Unit: 0.01A
        private const val COUNTDOWN_TIME = 65
        private const val ALERT_MESSAGE = 67
        private const val INPUT_MODE = 69 // 0x00 - in parallel, 0x01 - independent
        private const val COMMUNICATION_BOARD = 71
        private const val INSULATION_PV1_PLUS_TO_GROUND = 73
        private const val INSULATION_PV_MINUS_TO_GROUND = 75
        private const val COUNTRY = 77
    }
}
