package dev.drzepka.smarthome.logger.core.queue

data class QueueBatch<T>(val items: Collection<QueueItem<T>>) {
    val size: Int
        get() = items.size
}