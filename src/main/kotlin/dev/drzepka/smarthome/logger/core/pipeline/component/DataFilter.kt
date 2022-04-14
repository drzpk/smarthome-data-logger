package dev.drzepka.smarthome.logger.core.pipeline.component

import dev.drzepka.smarthome.logger.core.pipeline.PipelineContext

interface DataFilter<T> {
    fun start(context: PipelineContext) {}
    fun stop(context: PipelineContext) {}
    fun filter(data: T): T?
}
