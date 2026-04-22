package dev.drzepka.smarthome.logger.core.pipeline.component.datasource

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.pipeline.component.DataCollector
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import java.time.Duration

open class FixedRateDataSource<I>(
    name: String,
    private val interval: Duration,
    private val collector: DataCollector<I>,
    decoder: DataDecoder<I>
) : DataSource<I>(name, decoder) {

    private val log by Logger()
    private val taskName = "dataSource_$name"
    private lateinit var scheduler: TaskScheduler

    override fun start(scheduler: TaskScheduler) {
        log.info("Starting fixed data source '{}'", name)
        this.scheduler = scheduler
        scheduler.schedule(taskName, interval) {
            val data = collector.getData()
            forwardData(data)
        }
    }

    override fun stop() {
        log.info("Stopping fixed data source '{}'", name)
        scheduler.cancel(taskName)
    }
}
