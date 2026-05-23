package dev.drzepka.smarthome.logger.core.pipeline.component.datasource

import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import dev.drzepka.smarthome.logger.core.pipeline.component.DataListener
import dev.drzepka.smarthome.logger.core.scheduler.TaskScheduler
import dev.drzepka.smarthome.logger.core.util.Logger

open class ListenerDataSource<I>(
    name: String,
    private val listener: DataListener<I>,
    decoder: DataDecoder<I>
) : DataSource<I>(name, decoder) {

    private val log by Logger()
    private var started = false

    init {
        listener.dataSink = { data ->
            if (started)
                forwardData(data)
            else
                log.warn("Received data when listener should be stopped (data source {})", name)
        }
    }

    override fun start(scheduler: TaskScheduler) {
        log.info("Starting listener data source '{}'", name)
        listener.start()
        started = true
    }

    override fun stop() {
        log.info("Stopping listener data source '{}'", name)
        listener.stop()
        started = false
    }
}
