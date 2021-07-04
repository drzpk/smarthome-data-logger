package dev.drzepka.smarthome.logger

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader

abstract class DataLoggerModule(
    protected val configurationLoader: ConfigurationLoader,
    protected val scheduler: TaskScheduler
) {
    /** If true, module should operate on fake, random data. */
    var testMode = false

    /** Module name. */
    abstract val name: String

    /**
     * Initializes this module.
     * @return whether module should be started.
     */
    abstract fun initialize(): Boolean

    abstract fun start()

    abstract fun stop()
}