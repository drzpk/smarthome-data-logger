package dev.drzepka.smarthome.logger.sensors.model.config

import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader

class SensorsConfig private constructor(
    val serverUrl: String,
    val loggerId: Int,
    val loggerSecret: String
) {
    companion object {
        fun load(configurationLoader: ConfigurationLoader): SensorsConfig? {
            if (!configurationLoader.containsKey("sensors"))
                return null

            return SensorsConfig(
                configurationLoader.getValue("sensors.serverUrl", true)!!,
                configurationLoader.getInt("sensors.loggerId", true)!!,
                configurationLoader.getValue("sensors.loggerSecret", true)!!
            )
        }
    }
}