package dev.drzepka.smarthome.logger.core.config

import kotlin.time.DurationUnit
import kotlin.time.toDuration

class ServerProperties(source: ConfigPropertySource) {
    val serverUrl by lazy { source.getString("server.url") }
    val loggerId by lazy { source.getString("server.loggerId") }
    val loggerSecret by lazy { source.getString("server.loggerSecret") }
    val timeout = source.getInt("server.timeout", 3000).toDuration(DurationUnit.MILLISECONDS)
    val mock = source.getBoolean("server.mock", false)
}
