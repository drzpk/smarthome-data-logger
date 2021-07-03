package dev.drzepka.smarthome.logger.pvstats

import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader
import dev.drzepka.smarthome.logger.core.util.Logger
import dev.drzepka.smarthome.logger.core.util.roundAndGetDelay
import dev.drzepka.smarthome.logger.pvstats.model.config.PvStatsConfig
import dev.drzepka.smarthome.logger.pvstats.model.config.source.SourceConfigFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class PVStatsLogger {

    companion object {
        const val DEBUG = false

        private val loader = ConfigurationLoader()
        private val log by Logger()

        @JvmStatic
        fun main(args: Array<String>) {
            log.info("Loading configuration")

            val sourceLoggers = getSourceLoggers()
            if (sourceLoggers.isEmpty()) {
                log.info("No sources were found in configuration file, exiting")
                return
            }

            val executorService = Executors.newScheduledThreadPool(4)

            sourceLoggers.forEach { logger ->
                logger.getIntervals().forEach { interval ->
                    executorService.scheduleAtFixedRate(
                            { logger.execute(interval.key) },
                            roundAndGetDelay(interval.value).toLong(),
                            interval.value.toLong(),
                            TimeUnit.SECONDS
                    )
                }
            }

            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)
        }

        private fun getSourceLoggers(): List<SourceLogger> {
            val pvStatsConfig = PvStatsConfig.loadFromProperties(loader)
            val sourceNames = SourceConfigFactory.getAvailableNames(loader)

            return sourceNames.map {
                val config = SourceConfigFactory.createSourceConfig(it, loader)
                try {
                    SourceLogger(pvStatsConfig, config)
                } catch (e: Exception) {
                    log.error("Error while initializing logger {}", config.name, e)
                    exitProcess(1)
                }
            }
        }
    }
}