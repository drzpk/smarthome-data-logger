package dev.drzepka.smarthome.logger.core.pipeline.component

interface DataDecoder<I, O> {
    fun decode(item: I): Collection<O>

    companion object {
        fun <T> noop() = object : DataDecoder<T, T> {
            override fun decode(item: T): Collection<T> = listOf(item)
        }
    }
}
