package dev.drzepka.smarthome.logger.core.pipeline.component

import dev.drzepka.smarthome.logger.core.model.measurement.Measurement

interface DataDecoder<I> {
    fun decode(item: I): Collection<Measurement>

    companion object {
        fun <T> noop() = object : DataDecoder<Measurement> {
            override fun decode(item: Measurement): Collection<Measurement> = listOf(item)
        }
    }
}
