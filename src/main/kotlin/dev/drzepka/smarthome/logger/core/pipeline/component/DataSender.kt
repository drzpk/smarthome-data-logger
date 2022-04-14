package dev.drzepka.smarthome.logger.core.pipeline.component

import dev.drzepka.smarthome.logger.core.pipeline.PipelineContext
import dev.drzepka.smarthome.logger.core.queue.QueueItem

interface DataSender<T> {
    fun start(context: PipelineContext) {}
    fun stop(context: PipelineContext) {}
    suspend fun send(items: Collection<QueueItem<T>>)
}
