package dev.drzepka.smarthome.logger.core.queue

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.common.util.Mockable
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue

@Mockable
class LoggerQueue<T>(private val maxBatchSize: Int, private val maxAge: Duration, private val maxSize: Int = 15_000) {
    private val log by Logger()
    private val queue = ConcurrentLinkedQueue<QueueItem<T>>()

    fun size(): Int = queue.size

    fun enqueue(item: T) {
        if (queue.size >= maxSize) {
            queue.last()
            val oldest = queue.poll()
            log.warn(
                "Queue has maximum size of {} elements, dropping the oldest element from {}",
                maxSize,
                oldest.createdAt
            )
        }

        queue.add(QueueItem(item))
    }

    @Synchronized
    suspend fun processQueue(handler: (suspend (batch: Collection<QueueItem<T>>) -> ProcessingResult)) {
        do {
            val batch = getBatch()
            if (batch.isEmpty())
                break

            val result = processItem(handler, batch)
            var continueProcessing = result.`continue`

            when (result.status) {
                ProcessingResult.Status.OK -> {
                    // The AbstractCollection.removeAll() method uses the Collection.contains() method under the hood.
                    // That's why the getBatch() method generates a set.
                    queue.removeAll(batch)
                }
                ProcessingResult.Status.SERVER_UNAVAILABLE -> {
                    log.error(
                        "Processing of batch ({}; {}) finished with SERVER_UNAVAILABLE error",
                        batch.first().createdAt,
                        batch.size
                    )

                    // Continuation doesn't have any sense when server is unavailable because the following
                    // batches are likely to finish with exactly the same status.
                    continueProcessing = false
                }
            }

            if (!continueProcessing)
                break

        } while (batch.isNotEmpty())
    }

    private fun getBatch(): Collection<QueueItem<T>> {
        val batchSize = minOf(queue.size, maxBatchSize)
        val batch = LinkedHashSet<QueueItem<T>>(batchSize)

        val iterator = queue.iterator()
        while (batch.size < batchSize && iterator.hasNext()) {
            val current = iterator.next()

            if (isExpired(current)) {
                log.warn("Item {} has expired and will be removed from the queue.", current.createdAt)
                iterator.remove()
                continue
            }

            batch.add(current)
        }

        return batch
    }

    private fun isExpired(item: QueueItem<T>): Boolean = item.createdAt.plus(maxAge).isBefore(Instant.now())

    private suspend fun processItem(
        handler: (suspend (batch: Collection<QueueItem<T>>) -> ProcessingResult),
        batch: Collection<QueueItem<T>>
    ): ProcessingResult {
        return try {
            handler.invoke(batch)
        } catch (e: Exception) {
            throw IllegalStateException("Unhandled exception while processing batch", e)
        }
    }
}