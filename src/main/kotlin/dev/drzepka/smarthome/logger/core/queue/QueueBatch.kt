package dev.drzepka.smarthome.logger.core.queue

data class QueueBatch(val items: Collection<QueueItem>) {
    val size: Int
        get() = items.size
}