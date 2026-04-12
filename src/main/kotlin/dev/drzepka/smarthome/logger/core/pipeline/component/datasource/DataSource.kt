package dev.drzepka.smarthome.logger.core.pipeline.component.datasource

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.pipeline.PipelineContext
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder

abstract class DataSource<I, O>(val name: String, private val decoder: DataDecoder<I, O>) {
    var receiver: DataReceiver<O>? = null

    private val log by Logger()

    open fun start(context: PipelineContext) = Unit

    open fun stop(context: PipelineContext) = Unit

    protected open fun forwardData(data: Collection<I>) {
        if (receiver != null)
            receiver!!.onDataAvailable(decodeData(data))
        else
            log.warn("DataSourceListener is null")
    }

    private fun decodeData(data: Collection<I>): Collection<O> = data.flatMap { item ->
        try {
            decoder.decode(item)
        } catch (e: Exception) {
            log.error("Error while decoding data item", e)
            emptyList()
        }
    }
}
