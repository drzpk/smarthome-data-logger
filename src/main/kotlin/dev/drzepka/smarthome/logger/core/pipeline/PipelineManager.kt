package dev.drzepka.smarthome.logger.core.pipeline

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.pipeline.component.sender.DataSender
import dev.drzepka.smarthome.logger.core.scheduler.TaskScheduler

class PipelineManager(
    private val scheduler: TaskScheduler,
    private val dataSender: DataSender
) {
    private val log by Logger()
    private val pipelines = mutableSetOf<Pipeline>()

    private var running = true

    fun addPipeline(pipeline: Pipeline) {
        log.info("Adding pipeline '{}'", pipeline.name)
        val added = pipelines.add(pipeline)

        if (added && running)
            startPipeline(pipeline)
    }

    fun stop() {
        if (!running)
            return

        log.info("Stopping pipeline manager with {} pipeline(s)", pipelines.size)
        pipelines.forEach { stopPipeline(it) }

        running = false
    }

    private fun startPipeline(pipeline: Pipeline) {
        try {
            log.debug("Starting pipeline {}", pipeline.name)
            pipeline.start(scheduler, dataSender)
        } catch (e: Exception) {
            log.error("Error while starting pipeline {}", pipeline.name, e)
        }
    }

    private fun stopPipeline(pipeline: Pipeline) {
        try {
            log.debug("Stopping pipeline {}", pipeline.name)
            pipeline.stop()
        } catch (e: Exception) {
            log.error("Error while stopping pipeline {}", pipeline.name, e)
        }
    }
}
