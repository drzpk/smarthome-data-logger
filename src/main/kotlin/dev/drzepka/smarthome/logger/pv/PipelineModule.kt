package dev.drzepka.smarthome.logger.pv

import dev.drzepka.smarthome.logger.DataLoggerModule
import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.pipeline.PipelineRegistrar
import dev.drzepka.smarthome.logger.core.util.Logger

class PipelineModule(
    private val config: ConfigPropertySource,
    private val registrar: PipelineRegistrar
) : DataLoggerModule {

    private val log by Logger()

    override val name = "pipeline"
    override var testMode = false

    override suspend fun initialize(): Boolean {
        if (config.getKeys("source").isEmpty()) {
            log.info("No pipeline sources configured, skipping")
            return false
        }
        registrar.registerAll()
        return true
    }

    override suspend fun start() = Unit

    override suspend fun stop() = Unit
}
