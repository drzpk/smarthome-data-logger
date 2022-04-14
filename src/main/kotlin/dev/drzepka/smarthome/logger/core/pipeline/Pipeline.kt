package dev.drzepka.smarthome.logger.core.pipeline

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.common.util.Mockable
import dev.drzepka.smarthome.logger.core.executor.ConnectionException
import dev.drzepka.smarthome.logger.core.pipeline.component.DataFilter
import dev.drzepka.smarthome.logger.core.pipeline.component.DataSender
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.DataReceiver
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.DataSource
import dev.drzepka.smarthome.logger.core.queue.LoggerQueue
import dev.drzepka.smarthome.logger.core.queue.QueueBatch
import dev.drzepka.smarthome.logger.core.util.ExceptionTracker
import dev.drzepka.smarthome.logger.core.util.StopWatch
import dev.drzepka.smarthome.logger.core.util.suspendRunCatching
import java.time.Duration

@Mockable
@Suppress("LeakingThis")
class Pipeline<T>(
    val name: String,
    private val sendInterval: Duration,
    private val dataSender: DataSender<T>,
    private val queue: LoggerQueue<T> = LoggerQueue(30, Duration.ofHours(48))
) {
    private val log by Logger()
    private val filters = mutableListOf<DataFilter<T>>()
    private val dataSources = mutableListOf<DataSource<*, *>>()
    private val sendTaskName = "dataSend_$name"
    private val sendDataTracker = ExceptionTracker("SendData")

    private var running = false

    fun addDataSource(dataSource: DataSource<*, T>) {
        checkNotRunning()

        if (dataSources.contains(dataSource))
            throw IllegalArgumentException("Data source already added")

        dataSource.receiver = object : DataReceiver<T> {
            override fun onDataAvailable(items: Collection<T>) {
                this@Pipeline.onDataAvailable(dataSource, items)
            }
        }

        dataSources.add(dataSource)
    }

    fun addFilter(filter: DataFilter<T>) {
        checkNotRunning()

        if (filters.contains(filter))
            throw IllegalArgumentException("Filter already added")

        filters.add(filter)
    }

    fun start(context: PipelineContext) {
        if (running)
            return

        log.info(
            "Starting pipeline '{}' with {} data source(s). Send interval is {}",
            name,
            dataSources.size,
            sendInterval
        )

        context.scheduler.schedule(sendTaskName, sendInterval) {
            val timeLimit = sendInterval.minusSeconds(5L)
            sendData(timeLimit)
        }

        dataSender.start(context)
        dataSources.forEach { it.start(context) }
        filters.forEach { it.start(context) }

        running = true
    }

    fun stop(context: PipelineContext) {
        if (!running)
            return

        log.info("Stopping pipeline '{}' with {} data source(s)", name, dataSources.size)
        filters.forEach { it.stop(context) }
        dataSources.forEach { it.stop(context) }
        dataSender.stop(context)
        context.scheduler.cancel(sendTaskName)

        running = false
    }

    private fun onDataAvailable(dataSource: DataSource<*, T>, items: Collection<T>) {
        if (!running) {
            log.warn("Received data from source '{}' when it should be stopped", dataSource.name)
            return
        }

        items
            .mapNotNull(::filterItem)
            .forEach { queue.enqueue(it) }
    }

    private fun filterItem(item: T): T? {
        var filteredItem: T? = item
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

    private suspend fun sendData(timeLimit: Duration) {
        val message = "Error while sending data in pipeline $name"
        sendDataTracker.suspendRunCatching(log, message, 3) {
            doSendData(timeLimit)
        }
    }

    private suspend fun doSendData(timeLimit: Duration) {
        val stopWatch = StopWatch(true)
        var timeLimitExceeded = false

        while (queue.size() > 0 && !timeLimitExceeded) {
            val batch = queue.getBatch()
            log.trace("Processing {} items starting at {}", batch.size, batch.items.firstOrNull()?.createdAt)

            sendBatch(batch)
            if (stopWatch.current() > timeLimit) {
                stopWatch.stop()
                timeLimitExceeded = true

                log.warn(
                    "Some measurements ({}) couldn't be sent. Sending took {}, but time limit is {}",
                    queue.size(), stopWatch.elapsed(), timeLimit
                )
            }
        }
    }

    private suspend fun sendBatch(batch: QueueBatch<T>) {
        val result = runCatching {
            dataSender.send(batch.items)
        }

        // Don't remove batch because of connection failure.
        val exception = result.exceptionOrNull()
        if (exception is ConnectionException) {
            log.error("Cannot send {} measurements because of connection failure", batch.size)
        } else {
            queue.removeBatch(batch)
        }

        exception?.let { throw it }
    }

    private fun checkNotRunning() {
        if (running)
            throw IllegalStateException("Cannot modify pipeline state when it's running")
    }
}
