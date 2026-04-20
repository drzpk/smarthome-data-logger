package dev.drzepka.smarthome.logger.core.pipeline.component

interface DataCollector<T> {
    suspend fun getData(): Collection<T>
}
