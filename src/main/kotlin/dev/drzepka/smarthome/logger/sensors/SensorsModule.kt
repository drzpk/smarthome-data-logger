package dev.drzepka.smarthome.logger.sensors

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.DataLoggerModule
import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader
import dev.drzepka.smarthome.logger.core.pipeline.Pipeline
import dev.drzepka.smarthome.logger.core.pipeline.PipelineManager
import dev.drzepka.smarthome.logger.sensors.core.DeviceManager
import dev.drzepka.smarthome.logger.sensors.core.SensorsRequestExecutor
import dev.drzepka.smarthome.logger.sensors.model.config.SensorsConfig
import dev.drzepka.smarthome.logger.sensors.pipeline.SensorsDataSender
import dev.drzepka.smarthome.logger.sensors.pipeline.filter.DeviceFilter
import java.time.Duration

class SensorsModule(configurationLoader: ConfigurationLoader, scheduler: TaskScheduler) :
    DataLoggerModule(configurationLoader, scheduler) {

    override val name: String = "sensors"

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
        log.info("Starting sensors module")

        pipelineManager = PipelineManager(scheduler)
        pipelineManager.start()

        val requestExecutor = SensorsRequestExecutor(sensorsConfig, 3)
        val sender = SensorsDataSender(requestExecutor)

        val deviceManager = DeviceManager(requestExecutor)
        deviceManager.initialize()

        val sensorsPipeline = Pipeline("sensors", Duration.ofSeconds(30), sender)
        sensorsPipeline.addFilter(DeviceFilter(deviceManager))

        val dataSources = DataSourceFactory(deviceManager, testMode).createDataSources()
        dataSources.forEach { sensorsPipeline.addDataSource(it) }

        pipelineManager.addPipeline(sensorsPipeline)
    }

    override suspend fun stop() {
        log.info("Stopping sensors module")
        pipelineManager.stop()
    }
}
