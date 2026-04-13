package dev.drzepka.smarthome.logger.core.config

class SensorsProperties(source: ConfigPropertySource) {
    val serverUrl = source.getString("serverUrl")
    val loggerId = source.getInt("loggerId")
    val loggerSecret = source.getString("loggerSecret")
}
