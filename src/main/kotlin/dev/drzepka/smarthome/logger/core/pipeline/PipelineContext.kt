package dev.drzepka.smarthome.logger.core.pipeline

import dev.drzepka.smarthome.common.TaskScheduler

interface PipelineContext {
    val scheduler: TaskScheduler
}
