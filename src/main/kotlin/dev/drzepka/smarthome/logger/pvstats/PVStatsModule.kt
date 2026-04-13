package dev.drzepka.smarthome.logger.pvstats

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.DataLoggerModule
import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.pvstats.model.config.PvStatsConfig
import dev.drzepka.smarthome.logger.pvstats.model.config.source.AforeConfig
import dev.drzepka.smarthome.logger.pvstats.model.config.source.SourceConfigFactory
import java.time.Duration
import kotlin.system.exitProcess

class PVStatsModule(
    private val configPropertySource: ConfigPropertySource,
    private val scheduler: TaskScheduler
) : DataLoggerModule {

    override val name: String = "pvstats"
    override var testMode: Boolean = false

    private val log by Logger()
    private val sourceLoggers = ArrayList<SourceLogger>()

    override suspend fun initialize(): Boolean {
        if (configPropertySource.getKeys("pvstats").isEmpty()) {
            log.info("No pv-stats configuration was found")
            return false
        }

        loadSourceLoggers()
        if (sourceLoggers.isEmpty()) {
            log.info("No pv-stats sources were found in configuration file")
            return false
        }

        return true
    }

    override suspend fun start() {
        sourceLoggers.forEach { logger ->
            logger.getIntervals().forEach { interval ->
                val duration = Duration.ofSeconds(interval.value.toLong())
                val taskName = "pvstatsLogger_${logger.name}_${interval.key}"
                scheduler.schedule(taskName, duration) {
                    logger.execute(interval.key)
                }
            }
        }
    }

    override suspend fun stop() {
        // Nothing here
    }

    private fun loadSourceLoggers() {
        sourceLoggers.clear()

        val pvStatsConfig = PvStatsConfig.load(configPropertySource)
        val sourceNames = SourceConfigFactory.getAvailableNames(configPropertySource)

        val foundLoggers = sourceNames.mapNotNull {
            val config = SourceConfigFactory.createSourceConfig(it, configPropertySource)
            if (config is AforeConfig)
                return@mapNotNull null // handled in the new module

            try {
                SourceLogger(pvStatsConfig, config, testMode)
            } catch (e: Exception) {
                log.error("Error while initializing logger {}", config.name, e)
                exitProcess(1)
            }
        }

        sourceLoggers.addAll(foundLoggers)
    }
}
