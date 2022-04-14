package dev.drzepka.smarthome.logger.core.pipeline.component.datasource

interface DataReceiver<T> {
    fun onDataAvailable(items: Collection<T>)
}
