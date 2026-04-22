package dev.drzepka.smarthome.logger.sensors

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.DataLoggerModule
import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.device.DeviceManager
import dev.drzepka.smarthome.logger.core.pipeline.Pipeline
import dev.drzepka.smarthome.logger.core.pipeline.PipelineManager
import dev.drzepka.smarthome.logger.sensors.model.config.SensorsConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.time.Duration

class SensorsModule(
    private val configPropertySource: ConfigPropertySource
) : DataLoggerModule, KoinComponent {

    override val name: String = "sensors"
    override var testMode: Boolean = false

    private val log by Logger()

    override suspend fun initialize(): Boolean {
        if (SensorsConfig.load(configPropertySource) == null) {
            log.info("No sensors configuration found")
            return false
        }
        return true
    }

    override suspend fun start() {
        log.info("Starting sensors module")

        val pipelineManager = get<PipelineManager>()
        val deviceManager = get<DeviceManager>()
        deviceManager.initialize()

        // todo: drop measurements which aren't present in the devices list
        val sensorsPipeline = Pipeline("sensors", Duration.ofSeconds(30))

        val dataSources = DataSourceFactory(deviceManager, testMode).createDataSources()
        dataSources.forEach { sensorsPipeline.addDataSource(it) }

        pipelineManager.addPipeline(sensorsPipeline)
    }

    override suspend fun stop() {
        log.info("Stopping sensors module")
    }
}
