package dev.drzepka.smarthome.logger.core.pipeline.component

interface DataDecoder<I, O> {
    fun decode(data: I): List<O>
}
