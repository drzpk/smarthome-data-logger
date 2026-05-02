package dev.drzepka.smarthome.logger.core.pipeline.component.sender

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.config.ServerDataSenderProperties
import dev.drzepka.smarthome.logger.core.executor.ConnectionException
import dev.drzepka.smarthome.logger.core.model.measurement.CreateMeasurementsRequest
import dev.drzepka.smarthome.logger.core.model.measurement.Measurement
import dev.drzepka.smarthome.logger.core.queue.LoggerQueue
import dev.drzepka.smarthome.logger.core.queue.QueueItem
import dev.drzepka.smarthome.logger.core.transport.ServerRequestExecutor

class ServerDataSender(
    properties: ServerDataSenderProperties,
    private val executor: ServerRequestExecutor,
    scheduler: TaskScheduler
) : DataSender {
    private val log by Logger()
    private val queue = LoggerQueue(properties.maxBatchSize, properties.maxAge, properties.maxSize)
    private val errorTracker = ConnectionErrorTracker(
        "server", properties.errorThreshold, properties.throttleSkipCount,
        properties.throttleBackoffFactor, properties.maxThrottleSkipCount
    )

    init {
        scheduler.schedule("serverDataSender_send", properties.sendInterval) {
            sendQueued()
        }
    }

    override fun queue(items: Collection<Measurement>) {
        log.debug("Enqueueing {} measurements", items.size)
        items.forEach { queue.enqueue(it) }
    }

    override suspend fun send(items: Collection<Measurement>) {
        log.debug("Sending {} measurements to server immediately", items.size)
        try {
            doSend(items.map { QueueItem(it) })
            log.debug("Successfully sent {} measurements immediately", items.size)
        } catch (e: Exception) {
            log.debug("Failed to send {} measurements immediately, dropping: {}", items.size, e.message)
        }
    }

    private suspend fun sendQueued() {
        if (errorTracker.shouldSkip()) return

        try {
            while (queue.size() > 0) {
                val batch = queue.getBatch()
                if (batch.size == 0) break

                log.debug("Sending queued batch of {} measurements", batch.size)
                try {
                    doSend(batch.items)
                    log.debug("Successfully sent queued batch of {} measurements", batch.size)
                    errorTracker.recordSuccess()
                    queue.removeBatch(batch)
                } catch (_: ConnectionException) {
                    if (errorTracker.recordConnectionFailure())
                        log.debug("Cannot send batch of {} measurements due to connection failure, will retry later", batch.size)
                    break
                } catch (e: Exception) {
                    log.debug("Dropping batch of {} measurements due to error: {}", batch.size, e.message)
                    queue.removeBatch(batch)
                    break
                }
            }
        } catch (e: Exception) {
            log.debug("Unexpected error during queued send: {}", e.message)
        }
    }

    private suspend fun doSend(items: Collection<QueueItem>) {
        val measurements = items.map { it.content }
        executor.sendMeasurements(CreateMeasurementsRequest(measurements))
    }
}
