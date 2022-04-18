package dev.drzepka.smarthome.logger.core.pipeline

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.common.util.Logger

class PipelineManager(private val scheduler: TaskScheduler) {
    private val log by Logger()
    private val pipelines = mutableSetOf<Pipeline<*>>()
    private val context = ContextImpl()

    private var running = false

    fun addPipeline(pipeline: Pipeline<*>) {
        log.info("Adding pipeline '{}'", pipeline.name)
        val added = pipelines.add(pipeline)

        if (added && running)
            startPipeline(pipeline)
    }

    fun start() {
        if (running)
            return

        log.info("Starting pipeline manager with {} pipeline(s)", pipelines.size)
        pipelines.forEach { startPipeline(it) }

        running = true
    }

    fun stop() {
        if (!running)
            return

        log.info("Stopping pipeline manager with {} pipeline(s)", pipelines.size)
        pipelines.forEach { stopPipeline(it) }

        running = false
    }

    private fun startPipeline(pipeline: Pipeline<*>) {
        try {
            log.debug("Starting pipeline {}", pipeline.name)
            pipeline.start(context)
        } catch (e: Exception) {
            log.error("Error while starting pipeline {}", pipeline.name, e)
        }
    }

    private fun stopPipeline(pipeline: Pipeline<*>) {
        try {
            log.debug("Stopping pipeline {}", pipeline.name)
            pipeline.stop(context)
        } catch (e: Exception) {
            log.error("Error while stopping pipeline {}", pipeline.name, e)
        }
    }

    private inner class ContextImpl : PipelineContext {
        override val scheduler = this@PipelineManager.scheduler
    }
}
