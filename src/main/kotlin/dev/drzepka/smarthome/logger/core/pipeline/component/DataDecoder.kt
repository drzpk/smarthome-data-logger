package dev.drzepka.smarthome.logger.core.pipeline.component

interface DataDecoder<I, O> {
    fun decode(data: I): List<O>

    companion object {
        fun <T> noop() = object : DataDecoder<T, T> {
            override fun decode(data: T): List<T> = listOf(data)
        }
    }
}
