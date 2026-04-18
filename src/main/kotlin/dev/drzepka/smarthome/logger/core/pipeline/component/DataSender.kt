package dev.drzepka.smarthome.logger.core.pipeline.component

import dev.drzepka.smarthome.logger.core.queue.QueueItem

interface DataSender<T> {
    fun start() {}
    fun stop() {}
    suspend fun send(items: Collection<QueueItem<T>>)
}
