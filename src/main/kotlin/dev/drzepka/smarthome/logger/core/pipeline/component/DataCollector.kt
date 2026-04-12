package dev.drzepka.smarthome.logger.core.pipeline.component

interface DataCollector<T> {
    fun start() = Unit
    fun stop() = Unit
    suspend fun getData(): Collection<T>
}
