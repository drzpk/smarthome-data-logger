package dev.drzepka.smarthome.logger.core.pipeline.component.sender

import dev.drzepka.smarthome.logger.core.queue.QueueItem

interface DataSender {
    suspend fun send(items: Collection<QueueItem>)
}