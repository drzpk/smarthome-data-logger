package dev.drzepka.smarthome.logger.core.pipeline.component.datasource

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.pipeline.PipelineContext
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import dev.drzepka.smarthome.logger.core.pipeline.component.DataListener

open class ListenerDataSource<I, O>(
    name: String,
    private val listener: DataListener<I>,
    decoder: DataDecoder<I, O>
) : DataSource<I, O>(name, decoder) {

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

    override fun start(context: PipelineContext) {
        log.info("Starting listener data source '{}'", name)
        listener.start()
        started = true
    }

    override fun stop(context: PipelineContext) {
        log.info("Stopping listener data source '{}'", name)
        listener.stop()
        started = false
    }
}
