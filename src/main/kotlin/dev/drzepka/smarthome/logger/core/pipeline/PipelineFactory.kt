package dev.drzepka.smarthome.logger.core.pipeline

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.pvstats.model.config.SourceType

interface PipelineFactory {
    val sourceType: SourceType
    fun create(name: String, properties: ConfigPropertySource): Pipeline?
}
