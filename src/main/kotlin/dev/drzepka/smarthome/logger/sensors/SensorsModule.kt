package dev.drzepka.smarthome.logger.sensors

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.DataLoggerModule
import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader
import dev.drzepka.smarthome.logger.core.queue.LoggerQueue
import dev.drzepka.smarthome.logger.sensors.core.SensorsLoggerManager
import dev.drzepka.smarthome.logger.sensors.core.SensorsLoggerOrchestrator
import dev.drzepka.smarthome.logger.sensors.core.SensorsRequestExecutor
import dev.drzepka.smarthome.logger.sensors.model.config.SensorsConfig
import java.time.Duration

class SensorsModule(configurationLoader: ConfigurationLoader, scheduler: TaskScheduler) :
    DataLoggerModule(configurationLoader, scheduler) {

    override val name: String = "sensors"

    private val log by Logger()

    private lateinit var sensorsConfig: SensorsConfig
    private lateinit var requestExecutor: SensorsRequestExecutor
    private lateinit var orchestrator: SensorsLoggerOrchestrator

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
        requestExecutor = SensorsRequestExecutor(sensorsConfig, 3)

        val manager = SensorsLoggerManager(requestExecutor, LoggerQueue(30, Duration.ofHours(24)))
        orchestrator = SensorsLoggerOrchestrator(manager, scheduler, testMode)
        orchestrator.start()
    }

    override suspend fun stop() {
        orchestrator.stop()
    }
}