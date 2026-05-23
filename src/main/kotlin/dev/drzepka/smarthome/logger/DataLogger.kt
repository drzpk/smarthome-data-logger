package dev.drzepka.smarthome.logger

import dev.drzepka.smarthome.logger.core.coreModule
import dev.drzepka.smarthome.logger.core.pipeline.PipelineManager
import dev.drzepka.smarthome.logger.core.util.Logger
import dev.drzepka.smarthome.logger.pv.pvModule
import dev.drzepka.smarthome.logger.sensors.sensorsModule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.concurrent.thread
import kotlin.system.exitProcess

object DataLogger {

    private val log by Logger()

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val koin = startKoin {
            modules(coreModule, sensorsModule, pvModule)
        }.koin

        val allModules = koin.getAll<DataLoggerModule>()

        log.info("Starting data logger (available modules: {})", allModules.size)
        val activeModules = initializeModules(allModules)
        if (activeModules.isEmpty()) {
            log.info("No active modules, stopping the application")
            stopKoin()
            return@runBlocking
        }

        startModules(activeModules)
        log.info("All modules have been started")

        val shutdown = CompletableDeferred<Unit>()
        Runtime.getRuntime().addShutdownHook(thread(false) {
            log.info("Received shutdown hook")
            shutdown.complete(Unit)
        })

        shutdown.await()

        log.info("Stopping modules")
        stopModules(activeModules)
        koin.get<PipelineManager>().stop()
        stopKoin()
    }

    private suspend fun initializeModules(modules: Collection<DataLoggerModule>, ): Collection<DataLoggerModule> {
        return modules.filter { module ->
            val result = cricicalTryCatch("Error while initializing module ${module.name}") {
                log.info("Initializing module ${module.name}")
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
            } catch (_: Exception) {
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