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

    fun getBatch(): QueueBatch<T> {
        val batchSize = minOf(queue.size, maxBatchSize)
        val batchItems = LinkedHashSet<QueueItem<T>>(batchSize)

        val iterator = queue.iterator()
        while (batchItems.size < batchSize && iterator.hasNext()) {
            val current = iterator.next()

            if (isExpired(current)) {
                log.warn("Item {} has expired and will be removed from the queue.", current.createdAt)
                iterator.remove()
                continue
            }

            batchItems.add(current)
        }

        return QueueBatch(batchItems)
    }

    fun removeBatch(batch: QueueBatch<T>) {
        queue.removeAll(batch.items)
    }

    private fun isExpired(item: QueueItem<T>): Boolean = item.createdAt.plus(maxAge).isBefore(Instant.now())
}