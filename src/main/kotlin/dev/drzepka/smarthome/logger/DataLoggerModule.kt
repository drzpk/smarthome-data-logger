package dev.drzepka.smarthome.logger

interface DataLoggerModule {
    val name: String
    var testMode: Boolean

    /**
     * Initializes this module.
     * @return whether module should be started.
     */
    suspend fun initialize(): Boolean
    suspend fun start()
    suspend fun stop()
}
