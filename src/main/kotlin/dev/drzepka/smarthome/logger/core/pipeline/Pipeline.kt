package dev.drzepka.smarthome.logger.core.pipeline

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.common.util.Mockable
import dev.drzepka.smarthome.logger.core.model.measurement.Measurement
import dev.drzepka.smarthome.logger.core.pipeline.component.DataFilter
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.DataReceiver
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.DataSource
import dev.drzepka.smarthome.logger.core.pipeline.component.sender.DataSender
import dev.drzepka.smarthome.logger.core.scheduler.TaskScheduler

@Mockable
@Suppress("LeakingThis")
class Pipeline(val name: String) {
    private val log by Logger()
    private val filters = mutableListOf<DataFilter>()
    private val dataSources = mutableListOf<DataSource<*>>()

    private lateinit var dataSender: DataSender
    private var running = false

    fun addDataSource(dataSource: DataSource<*>) {
        checkNotRunning()

        if (dataSources.contains(dataSource))
            throw IllegalArgumentException("Data source already added")

        dataSource.receiver = object : DataReceiver {
            override fun onDataAvailable(items: Collection<Measurement>) {
                this@Pipeline.onDataAvailable(dataSource, items)
            }
        }

        dataSources.add(dataSource)
    }

    fun addFilter(filter: DataFilter) {
        checkNotRunning()

        if (filters.contains(filter))
            throw IllegalArgumentException("Filter already added")

        filters.add(filter)
    }

    fun start(scheduler: TaskScheduler, dataSender: DataSender) {
        if (running)
            return

        this.dataSender = dataSender

        log.info("Starting pipeline '{}' with {} data source(s)", name, dataSources.size)

        dataSources.forEach { it.start(scheduler) }

        running = true
    }

    fun stop() {
        if (!running)
            return

        log.info("Stopping pipeline '{}' with {} data source(s)", name, dataSources.size)
        dataSources.forEach { it.stop() }

        running = false
    }

    private fun onDataAvailable(dataSource: DataSource<*>, items: Collection<Measurement>) {
        if (!running) {
            log.warn("Received data from source '{}' when it should be stopped", dataSource.name)
            return
        }

        val filtered = items.mapNotNull(::filterItem)
        dataSender.queue(filtered)
    }

    private fun filterItem(item: Measurement): Measurement? {
        var filteredItem: Measurement? = item
        for (filter in filters) {
            if (filteredItem == null)
                break

            val result = runCatching {
                filteredItem = filter.filter(filteredItem!!)
            }

            if (result.isFailure) {
                filteredItem = null
                log.error("Error while filtering an item", result.exceptionOrNull())
                break
            }
        }

        return filteredItem
    }

    private fun checkNotRunning() {
        if (running)
            throw IllegalStateException("Cannot modify pipeline state when it's running")
    }
}
