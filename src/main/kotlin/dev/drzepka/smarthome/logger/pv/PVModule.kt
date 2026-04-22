package dev.drzepka.smarthome.logger.pv

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.DataLoggerModule
import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.device.DeviceManager
import dev.drzepka.smarthome.logger.core.pipeline.PipelineManager
import dev.drzepka.smarthome.logger.core.pipeline.PipelineRegistrar
import dev.drzepka.smarthome.logger.pv.source.afore.AforeT6PipelineFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class PVModule(
    private val configPropertySource: ConfigPropertySource,
    private val deviceManager: DeviceManager,
    private val pipelineManager: PipelineManager
) : DataLoggerModule, KoinComponent {

    override val name: String = "pv"
    override var testMode: Boolean = false

    private val log by Logger()

    override suspend fun initialize(): Boolean {
        if (!PVProperties(configPropertySource).enabled) {
            log.info("PV module is disabled")
            return false
        }
        return true
    }

    override suspend fun start() {
        log.info("Starting PV module")
        deviceManager.initialize()
        val pvConfig = configPropertySource.getChild("pv")
        PipelineRegistrar(pvConfig, listOf(get<AforeT6PipelineFactory>()), pipelineManager).registerAll()
    }

    override suspend fun stop() {
        log.info("Stopping PV module")
    }
}
