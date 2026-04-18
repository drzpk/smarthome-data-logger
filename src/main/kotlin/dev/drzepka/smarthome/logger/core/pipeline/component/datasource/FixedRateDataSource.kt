package dev.drzepka.smarthome.logger.core.pipeline.component.datasource

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.pipeline.component.DataCollector
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import java.time.Duration

open class FixedRateDataSource<I, O>(
    name: String,
    private val interval: Duration,
    private val scheduler: TaskScheduler,
    private val collector: DataCollector<I>,
    decoder: DataDecoder<I, O>
) : DataSource<I, O>(name, decoder) {

    private val log by Logger()
    private val taskName = "dataSource_$name"

    override fun start() {
        log.info("Starting fixed data source '{}'", name)
        scheduler.schedule(taskName, interval) {
            val data = collector.getData()
            forwardData(data)
        }
        collector.start()
    }

    override fun stop() {
        log.info("Stopping fixed data source '{}'", name)
        collector.stop()
        scheduler.cancel(taskName)
    }
}
