package dev.drzepka.smarthome.logger.core.pipeline.component

interface DataFilter<T> {
    fun start() {}
    fun stop() {}
    fun filter(data: T): T?
}
