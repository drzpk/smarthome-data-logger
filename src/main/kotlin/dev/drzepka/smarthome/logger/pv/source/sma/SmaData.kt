package dev.drzepka.smarthome.logger.pv.source.sma

import java.time.Instant

data class SmaData(
    val time: Instant,
    val values: Map<SmaField, Double>
)
