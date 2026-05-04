package dev.drzepka.smarthome.logger.pv.source.sma

import java.time.Instant

sealed class SmaData {
    abstract val time: Instant

    data class Metrics(
        override val time: Instant,
        val power: Int
    ) : SmaData()

    data class Measurement(
        override val time: Instant,
        val energyWh: Int
    ) : SmaData()
}
