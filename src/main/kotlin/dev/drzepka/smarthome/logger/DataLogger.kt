package dev.drzepka.smarthome.logger

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader
import dev.drzepka.smarthome.logger.pvstats.PVStatsModule
import dev.drzepka.smarthome.logger.sensors.SensorsModule
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.system.exitProcess

object DataLogger {

    private val log by Logger()

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val testMode = System.getProperty("TEST") != null

        val configurationLoader = ConfigurationLoader()
        val scheduler = TaskScheduler(8)

        val allModules = listOf(
            PVStatsModule(configurationLoader, scheduler),
            SensorsModule(configurationLoader, scheduler)
        )

        log.info("Starting data logger (available modules: {})", allModules.size)
        val activeModules = initializeModules(allModules, testMode)
        if (activeModules.isEmpty()) {
            log.info("No active modules, stopping the application")
            return@runBlocking
        }

        startModules(activeModules)
        log.info("All modules have been started")

        var running = true
        Runtime.getRuntime().addShutdownHook(thread(false) {
            log.info("Received shutdown hook")
            running = false
        })

        while (running) {
            Thread.sleep(1000)
        }

        log.info("Stopping modules")
        stopModules(activeModules)
    }

    private suspend fun initializeModules(
        modules: Collection<DataLoggerModule>,
        testMode: Boolean
    ): Collection<DataLoggerModule> {
        return modules.filter { module ->
            val result = cricicalTryCatch("Error while initializing module ${module.name}") {
                log.info("Initializing module ${module.name}")
                module.testMode = testMode
                module.initialize()
            }

            val status = if (result) "enabled" else "disabled"
            log.info("Initialization of module ${module.name} completed. Module status: {}", status)

            result
        }
    }

    private suspend fun startModules(modules: Collection<DataLoggerModule>) {
        modules.forEach { module ->
            cricicalTryCatch("Error while starting module ${module.name}") {
                module.start()
            }
        }
    }

    private suspend fun stopModules(modules: Collection<DataLoggerModule>) {
        modules.forEach {
            try {
                it.stop()
            } catch (e: Exception) {
                log.error("Error wihle stopping module {}", it.name)
            }
        }
    }

    private suspend fun <T> cricicalTryCatch(errorMessage: String, block: (suspend () -> T)): T {
        try {
            return block.invoke()
        } catch (e: Exception) {
            log.error(errorMessage, e)
            log.error("This is a critical error, stopping the application")
            exitProcess(1)
        }
    }
}