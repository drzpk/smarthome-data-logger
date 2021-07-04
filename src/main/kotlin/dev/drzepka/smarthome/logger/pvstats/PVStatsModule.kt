package dev.drzepka.smarthome.logger.pvstats

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.DataLoggerModule
import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader
import dev.drzepka.smarthome.logger.pvstats.model.config.PvStatsConfig
import dev.drzepka.smarthome.logger.pvstats.model.config.source.SourceConfigFactory
import java.time.Duration
import kotlin.system.exitProcess

class PVStatsModule(configurationLoader: ConfigurationLoader, scheduler: TaskScheduler) :
    DataLoggerModule(configurationLoader, scheduler) {

    override val name: String = "pvstats"

    private val log by Logger()
    private val sourceLoggers = ArrayList<SourceLogger>()

    override fun initialize(): Boolean {
        loadSourceLoggers()
        if (sourceLoggers.isEmpty()) {
            log.info("No pv-stats sources were found in configuration file")
            return false
        }

        return true
    }

    override fun start() {
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

    override fun stop() {
        // Nothing here
    }

    private fun loadSourceLoggers() {
        sourceLoggers.clear()

        val pvStatsConfig = PvStatsConfig.loadFromProperties(configurationLoader)
        val sourceNames = SourceConfigFactory.getAvailableNames(configurationLoader)

        val foundLoggers = sourceNames.map {
            val config = SourceConfigFactory.createSourceConfig(it, configurationLoader)
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