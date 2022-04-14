package dev.drzepka.smarthome.logger.core.pipeline.component

interface DataCollector<T> {
    fun start()
    fun stop()
    fun getData(): Collection<T>
}
