package dev.drzepka.smarthome.logger.pv

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.DataLoggerModule
import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.device.DeviceManager
import dev.drzepka.smarthome.logger.core.pipeline.Pipeline
import dev.drzepka.smarthome.logger.core.pipeline.PipelineManager
import dev.drzepka.smarthome.logger.test.TestSender
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.time.Duration

class PVModule(
    private val configPropertySource: ConfigPropertySource,
    private val deviceManager: DeviceManager,
    private val pipelineManager: PipelineManager,
    private val scheduler: TaskScheduler
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

        val pvPipeline = Pipeline("pv", Duration.ofSeconds(10), TestSender(), scheduler)

        val dataSources = get<PvDataSourceFactory>().createDataSources()
        dataSources.forEach { pvPipeline.addDataSource(it) }

        pipelineManager.addPipeline(pvPipeline)
    }

    override suspend fun stop() {
        log.info("Stopping PV module")
    }
}
