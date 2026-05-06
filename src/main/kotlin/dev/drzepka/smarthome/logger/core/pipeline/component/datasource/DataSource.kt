package dev.drzepka.smarthome.logger.core.pipeline.component.datasource

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.model.measurement.Measurement
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import dev.drzepka.smarthome.logger.core.scheduler.TaskScheduler

abstract class DataSource<I>(val name: String, private val decoder: DataDecoder<I>) {
    var receiver: DataReceiver? = null

    private val log by Logger()

    open fun start(scheduler: TaskScheduler) = Unit

    open fun stop() = Unit

    protected open fun forwardData(data: Collection<I>) {
        if (receiver != null)
            receiver!!.onDataAvailable(decodeData(data))
        else
            log.warn("DataSourceListener is null")
    }

    private fun decodeData(data: Collection<I>): Collection<Measurement> = data.flatMap { item ->
        try {
            decoder.decode(item)
        } catch (e: Exception) {
            log.error("Error while decoding data item", e)
            emptyList()
        }
    }
}
