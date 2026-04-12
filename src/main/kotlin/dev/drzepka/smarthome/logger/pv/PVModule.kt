package dev.drzepka.smarthome.logger.pv

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.DataLoggerModule
import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader
import dev.drzepka.smarthome.logger.core.pipeline.Pipeline
import dev.drzepka.smarthome.logger.core.pipeline.PipelineManager
import dev.drzepka.smarthome.logger.sensors.core.DeviceManager
import dev.drzepka.smarthome.logger.sensors.core.SensorsRequestExecutor
import dev.drzepka.smarthome.logger.sensors.model.config.SensorsConfig
import dev.drzepka.smarthome.logger.test.TestSender
import java.time.Duration

class PVModule(configurationLoader: ConfigurationLoader, scheduler: TaskScheduler) :
    DataLoggerModule(configurationLoader, scheduler) {

    override val name: String = "pv"

    private val log by Logger()

    private lateinit var sensorsConfig: SensorsConfig
    private lateinit var pipelineManager: PipelineManager

    override suspend fun initialize(): Boolean {
        val config = SensorsConfig.load(configurationLoader)
        if (config == null) {
            log.info("No sensors configuration found")
            return false
        }

        sensorsConfig = config
        return true
    }

    override suspend fun start() {
        log.info("Starting PV module")

        pipelineManager = PipelineManager(scheduler)
        pipelineManager.start()

        val requestExecutor = SensorsRequestExecutor(sensorsConfig, 3)
        val sender = TestSender()

        val deviceManager = DeviceManager(requestExecutor)
        deviceManager.initialize()

        val pvPipeline = Pipeline("pv", Duration.ofSeconds(10), sender)

        val dataSources = PvDataSourceFactory(deviceManager, configurationLoader).createDataSources()
        dataSources.forEach { pvPipeline.addDataSource(it) }

        pipelineManager.addPipeline(pvPipeline)
    }

    override suspend fun stop() {
        log.info("Stopping PV module")
        pipelineManager.stop()
    }
}