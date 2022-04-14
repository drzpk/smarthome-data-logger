package dev.drzepka.smarthome.logger.core.pipeline.component.datasource

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.pipeline.PipelineContext
import dev.drzepka.smarthome.logger.core.pipeline.component.DataCollector
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import java.time.Duration

open class FixedRateDataSource<I, O>(
    name: String,
    private val interval: Duration,
    private val collector: DataCollector<I>,
    decoder: DataDecoder<I, O>
) : DataSource<I, O>(name, decoder) {

    private val log by Logger()
    private val taskName = "dataSource_$name"

    override fun start(context: PipelineContext) {
        log.info("Starting fixed data source '{}'", name)
        context.scheduler.schedule(taskName, interval) {
            val data = collector.getData()
            forwardData(data)
        }
        collector.start()
    }

    override fun stop(context: PipelineContext) {
        log.info("Stopping fixed data source '{}'", name)
        collector.stop()
        context.scheduler.cancel(taskName)
    }
}
