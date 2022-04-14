package dev.drzepka.smarthome.logger.core.pipeline.component

abstract class DataListener<T> {
    lateinit var dataSink: (data: Collection<T>) -> Unit

    abstract fun start()
    abstract fun stop()

    fun onDataReceived(data: T) {
        onDataReceived(listOf(data))
    }

    fun onDataReceived(data: Collection<T>) {
        if (!this::dataSink.isInitialized)
            throw IllegalStateException("Data sink wasn't initialized.")

        dataSink.invoke(data)
    }
}